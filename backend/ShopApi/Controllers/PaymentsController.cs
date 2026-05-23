using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using ShopApi.Dtos;
using ShopApi.Services;

namespace ShopApi.Controllers;

[ApiController]
[Route("api/payments")]
public class PaymentsController : ControllerBase
{
    private readonly IPaymentService _payments;

    public PaymentsController(IPaymentService payments)
    {
        _payments = payments;
    }

    [AllowAnonymous]
    [HttpPost("sepay/webhook")]
    public async Task<IActionResult> SepayWebhook(SepayWebhookRequest request)
    {
        var result = await _payments.HandleSepayWebhookAsync(
            request,
            Request.Headers.Authorization.ToString());

        if (result == SepayWebhookResult.Unauthorized)
        {
            return Unauthorized(new { success = false, message = "Invalid webhook API key." });
        }

        return Ok(new { success = true });
    }
}
