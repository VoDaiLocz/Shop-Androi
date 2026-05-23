using System.Security.Cryptography;
using System.Text;
using System.Text.RegularExpressions;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using ShopApi.Data;
using ShopApi.Dtos;
using ShopApi.Models;

namespace ShopApi.Controllers;

[ApiController]
[Route("api/payments")]
public class PaymentsController : ControllerBase
{
    private const string PaymentMethodSepay = "SEPAY";
    private const string PaymentStatusPaid = "Paid";
    private const string PaymentStatusFailed = "Failed";
    private static readonly Regex PaymentCodePattern = new(@"ODD\d{6}", RegexOptions.Compiled | RegexOptions.IgnoreCase);

    private readonly ShopDbContext _db;
    private readonly IConfiguration _configuration;
    private readonly IWebHostEnvironment _environment;

    public PaymentsController(
        ShopDbContext db,
        IConfiguration configuration,
        IWebHostEnvironment environment)
    {
        _db = db;
        _configuration = configuration;
        _environment = environment;
    }

    [AllowAnonymous]
    [HttpPost("sepay/webhook")]
    public async Task<IActionResult> SepayWebhook(SepayWebhookRequest request)
    {
        if (!IsWebhookAuthorized())
        {
            return Unauthorized(new { success = false, message = "Invalid webhook API key." });
        }

        if (request.TransferType?.Equals("in", StringComparison.OrdinalIgnoreCase) != true)
        {
            return Ok(new { success = true });
        }

        var paymentCode = ResolvePaymentCode(request);
        if (paymentCode is null)
        {
            return Ok(new { success = true });
        }

        var order = await _db.Orders
            .Include(order => order.Items)
            .ThenInclude(item => item.Product)
            .SingleOrDefaultAsync(order => order.PaymentCode == paymentCode);

        if (order is null || !order.PaymentMethod.Equals(PaymentMethodSepay, StringComparison.OrdinalIgnoreCase))
        {
            return Ok(new { success = true });
        }

        if (order.PaymentStatus == PaymentStatusPaid)
        {
            return Ok(new { success = true });
        }

        await using var transaction = await _db.Database.BeginTransactionAsync();

        if (await IsTransactionProcessed(request.Id))
        {
            return Ok(new { success = true });
        }

        if (request.TransferAmount != order.TotalPrice || !HasEnoughStock(order))
        {
            MarkPaymentFailed(order, request);
            await _db.SaveChangesAsync();
            await transaction.CommitAsync();
            return Ok(new { success = true });
        }

        CompletePaidOrder(order, request);
        await RemovePaidCartItems(order);
        await _db.SaveChangesAsync();
        await transaction.CommitAsync();

        return Ok(new { success = true });
    }

    private bool IsWebhookAuthorized()
    {
        var expectedKey = _configuration["Sepay:WebhookApiKey"];
        if (string.IsNullOrWhiteSpace(expectedKey))
        {
            return _environment.IsDevelopment();
        }

        var actualHeader = Request.Headers.Authorization.ToString();
        var actualKey = actualHeader.StartsWith("Apikey ", StringComparison.OrdinalIgnoreCase)
            ? actualHeader["Apikey ".Length..].Trim()
            : actualHeader.Trim();

        return FixedTimeEquals(actualKey, expectedKey);
    }

    private static bool FixedTimeEquals(string actual, string expected)
    {
        var actualBytes = Encoding.UTF8.GetBytes(actual);
        var expectedBytes = Encoding.UTF8.GetBytes(expected);
        return actualBytes.Length == expectedBytes.Length &&
               CryptographicOperations.FixedTimeEquals(actualBytes, expectedBytes);
    }

    private static string? ResolvePaymentCode(SepayWebhookRequest request)
    {
        var directCode = request.Code?.Trim();
        if (!string.IsNullOrWhiteSpace(directCode))
        {
            return directCode.ToUpperInvariant();
        }

        var match = PaymentCodePattern.Match(request.Content ?? string.Empty);
        return match.Success ? match.Value.ToUpperInvariant() : null;
    }

    private async Task<bool> IsTransactionProcessed(long transactionId)
    {
        var value = transactionId.ToString();
        return await _db.Orders.AnyAsync(order => order.SepayTransactionId == value);
    }

    private static bool HasEnoughStock(Order order)
    {
        return order.Items.All(item => item.Product.Quantity >= item.Quantity);
    }

    private static void MarkPaymentFailed(Order order, SepayWebhookRequest request)
    {
        order.PaymentStatus = PaymentStatusFailed;
        order.SepayTransactionId = request.Id.ToString();
        order.SepayReferenceCode = request.ReferenceCode;
        order.UpdatedAt = DateTime.UtcNow;
    }

    private static void CompletePaidOrder(Order order, SepayWebhookRequest request)
    {
        foreach (var item in order.Items)
        {
            item.Product.Quantity -= item.Quantity;
        }

        order.PaymentStatus = PaymentStatusPaid;
        order.SepayTransactionId = request.Id.ToString();
        order.SepayReferenceCode = request.ReferenceCode;
        order.PaidAt = DateTime.UtcNow;
        order.UpdatedAt = DateTime.UtcNow;
    }

    private async Task RemovePaidCartItems(Order order)
    {
        var productIds = order.Items.Select(item => item.ProductId).ToList();
        await _db.CartItems
            .Where(item => item.UserId == order.UserId && productIds.Contains(item.ProductId))
            .ExecuteDeleteAsync();
    }
}
