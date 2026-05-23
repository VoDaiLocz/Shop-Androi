namespace ShopApi.Services;

public enum ServiceErrorType
{
    BadRequest,
    Conflict,
    NotFound,
    Forbidden
}

public sealed record ServiceError(ServiceErrorType Type, string Message);

public sealed record ServiceResult<T>(T? Value, ServiceError? Error)
{
    public bool Succeeded => Error is null;

    public static ServiceResult<T> Success(T value)
    {
        return new ServiceResult<T>(value, null);
    }

    public static ServiceResult<T> BadRequest(string message)
    {
        return Failure(ServiceErrorType.BadRequest, message);
    }

    public static ServiceResult<T> Conflict(string message)
    {
        return Failure(ServiceErrorType.Conflict, message);
    }

    public static ServiceResult<T> NotFound(string message)
    {
        return Failure(ServiceErrorType.NotFound, message);
    }

    public static ServiceResult<T> Forbidden()
    {
        return Failure(ServiceErrorType.Forbidden, string.Empty);
    }

    private static ServiceResult<T> Failure(ServiceErrorType type, string message)
    {
        return new ServiceResult<T>(default, new ServiceError(type, message));
    }
}
