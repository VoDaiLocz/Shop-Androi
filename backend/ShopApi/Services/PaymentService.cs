using System.Security.Cryptography;
using System.Text;
using System.Text.RegularExpressions;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Storage;
using ShopApi.Data;
using ShopApi.Dtos;
using ShopApi.Models;

namespace ShopApi.Services;

public class PaymentService : IPaymentService
{
    private const string PaymentMethodSepay = "SEPAY";
    private const string PaymentStatusPaid = "Paid";
    private const string PaymentStatusFailed = "Failed";
    private static readonly Regex PaymentCodePattern = new(@"ODD\d{6}", RegexOptions.Compiled | RegexOptions.IgnoreCase);

    private readonly ShopDbContext _db;
    private readonly IConfiguration _configuration;
    private readonly IWebHostEnvironment _environment;

    public PaymentService(
        ShopDbContext db,
        IConfiguration configuration,
        IWebHostEnvironment environment)
    {
        _db = db;
        _configuration = configuration;
        _environment = environment;
    }

    public async Task<SepayWebhookResult> HandleSepayWebhookAsync(
        SepayWebhookRequest request,
        string authorizationHeader)
    {
        if (!IsWebhookAuthorized(authorizationHeader))
        {
            return SepayWebhookResult.Unauthorized;
        }

        var paymentCode = ResolveIncomingPaymentCode(request);
        if (paymentCode is null)
        {
            return SepayWebhookResult.Accepted;
        }

        var order = await GetSepayOrderAsync(paymentCode);
        if (ShouldIgnoreOrder(order))
        {
            return SepayWebhookResult.Accepted;
        }

        await ApplyPaymentAsync(order!, request);
        return SepayWebhookResult.Accepted;
    }

    private bool IsWebhookAuthorized(string authorizationHeader)
    {
        var expectedKey = _configuration["Sepay:WebhookApiKey"];
        if (string.IsNullOrWhiteSpace(expectedKey))
        {
            return _environment.IsDevelopment();
        }

        var actualKey = ParseApiKey(authorizationHeader);
        return FixedTimeEquals(actualKey, expectedKey);
    }

    private static string ParseApiKey(string authorizationHeader)
    {
        return authorizationHeader.StartsWith("Apikey ", StringComparison.OrdinalIgnoreCase)
            ? authorizationHeader["Apikey ".Length..].Trim()
            : authorizationHeader.Trim();
    }

    private static bool FixedTimeEquals(string actual, string expected)
    {
        var actualBytes = Encoding.UTF8.GetBytes(actual);
        var expectedBytes = Encoding.UTF8.GetBytes(expected);
        return actualBytes.Length == expectedBytes.Length &&
               CryptographicOperations.FixedTimeEquals(actualBytes, expectedBytes);
    }

    private static string? ResolveIncomingPaymentCode(SepayWebhookRequest request)
    {
        if (request.TransferType?.Equals("in", StringComparison.OrdinalIgnoreCase) != true)
        {
            return null;
        }

        return ResolvePaymentCode(request);
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

    private async Task<Order?> GetSepayOrderAsync(string paymentCode)
    {
        return await _db.Orders
            .Include(order => order.Items)
            .ThenInclude(item => item.Product)
            .SingleOrDefaultAsync(order => order.PaymentCode == paymentCode);
    }

    private static bool ShouldIgnoreOrder(Order? order)
    {
        return order is null ||
               !order.PaymentMethod.Equals(PaymentMethodSepay, StringComparison.OrdinalIgnoreCase) ||
               order.PaymentStatus == PaymentStatusPaid;
    }

    private async Task ApplyPaymentAsync(Order order, SepayWebhookRequest request)
    {
        await using var transaction = await _db.Database.BeginTransactionAsync();

        if (await IsTransactionProcessedAsync(request.Id))
        {
            return;
        }

        if (!IsPaymentValid(order, request))
        {
            MarkPaymentFailed(order, request);
            await SavePaymentAsync(transaction);
            return;
        }

        CompletePaidOrder(order, request);
        await RemovePaidCartItemsAsync(order);
        await SavePaymentAsync(transaction);
    }

    private async Task<bool> IsTransactionProcessedAsync(long transactionId)
    {
        var value = transactionId.ToString();
        return await _db.Orders.AnyAsync(order => order.SepayTransactionId == value);
    }

    private static bool IsPaymentValid(Order order, SepayWebhookRequest request)
    {
        return request.TransferAmount == order.TotalPrice && HasEnoughStock(order);
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

    private async Task RemovePaidCartItemsAsync(Order order)
    {
        var productIds = order.Items.Select(item => item.ProductId).ToList();
        await _db.CartItems
            .Where(item => item.UserId == order.UserId && productIds.Contains(item.ProductId))
            .ExecuteDeleteAsync();
    }

    private async Task SavePaymentAsync(IDbContextTransaction transaction)
    {
        await _db.SaveChangesAsync();
        await transaction.CommitAsync();
    }
}
