namespace ShopApi.Models;

public class Order
{
    public int Id { get; set; }
    public int UserId { get; set; }
    public DateTime OrderDate { get; set; } = DateTime.UtcNow;
    public decimal TotalPrice { get; set; }
    public string Status { get; set; } = "Pending";
    public required string Address { get; set; }
    public required string PhoneNumber { get; set; }
    public string PaymentMethod { get; set; } = "COD";
    public string PaymentStatus { get; set; } = "Pending";
    public string? PaymentCode { get; set; }
    public string? SepayTransactionId { get; set; }
    public string? SepayReferenceCode { get; set; }
    public DateTime? PaidAt { get; set; }
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
    public DateTime? UpdatedAt { get; set; }

    public User User { get; set; } = null!;
    public ICollection<OrderItem> Items { get; set; } = new List<OrderItem>();
}
