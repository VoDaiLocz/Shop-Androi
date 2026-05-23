using ShopApi.Dtos;

namespace ShopApi.Services;

public interface IOrderService
{
    Task<ServiceResult<OrderResponse>> CreateAsync(int userId, CreateOrderRequest request);
    Task<List<OrderResponse>> GetUserOrdersAsync(int userId);
    Task<List<OrderResponse>> GetAllAsync();
    Task<ServiceResult<OrderPaymentStatusResponse>> GetPaymentStatusAsync(int userId, bool isAdmin, int orderId);
    Task<ServiceResult<OrderResponse>> UpdateStatusAsync(int orderId, UpdateOrderStatusRequest request);
}
