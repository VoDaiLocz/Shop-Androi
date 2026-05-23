using ShopApi.Dtos;

namespace ShopApi.Services;

public enum SepayWebhookResult
{
    Accepted,
    Unauthorized
}

public interface IPaymentService
{
    Task<SepayWebhookResult> HandleSepayWebhookAsync(
        SepayWebhookRequest request,
        string authorizationHeader);
}
