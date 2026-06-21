# ── Build stage ──────────────────────────────────────────────────
FROM mcr.microsoft.com/dotnet/sdk:9.0 AS build
WORKDIR /src

# Copy csproj first → cache NuGet restore
COPY ShopApi.csproj .
RUN dotnet restore

# Copy everything else and publish
COPY . .
RUN dotnet publish -c Release -o /app/publish

# ── Runtime stage ────────────────────────────────────────────────
FROM mcr.microsoft.com/dotnet/aspnet:9.0 AS runtime
WORKDIR /app

# Render uses PORT env var (default 10000)
ENV ASPNETCORE_URLS=http://+:${PORT:-10000}
ENV ASPNETCORE_ENVIRONMENT=Production

COPY --from=build /app/publish .

# Ensure seed images are available in the published output
# (they are already inside wwwroot via the publish step)

EXPOSE 10000

ENTRYPOINT ["dotnet", "ShopApi.dll"]
