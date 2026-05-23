using System;
using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace ShopApi.Migrations
{
    /// <inheritdoc />
    public partial class AddSepayPayments : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<DateTime>(
                name: "PaidAt",
                table: "Orders",
                type: "datetime(6)",
                nullable: true);

            migrationBuilder.AddColumn<string>(
                name: "PaymentCode",
                table: "Orders",
                type: "varchar(40)",
                maxLength: 40,
                nullable: true)
                .Annotation("MySql:CharSet", "utf8mb4");

            migrationBuilder.AddColumn<string>(
                name: "PaymentStatus",
                table: "Orders",
                type: "varchar(20)",
                maxLength: 20,
                nullable: false,
                defaultValue: "Pending")
                .Annotation("MySql:CharSet", "utf8mb4");

            migrationBuilder.AddColumn<string>(
                name: "SepayReferenceCode",
                table: "Orders",
                type: "varchar(120)",
                maxLength: 120,
                nullable: true)
                .Annotation("MySql:CharSet", "utf8mb4");

            migrationBuilder.AddColumn<string>(
                name: "SepayTransactionId",
                table: "Orders",
                type: "varchar(64)",
                maxLength: 64,
                nullable: true)
                .Annotation("MySql:CharSet", "utf8mb4");

            migrationBuilder.CreateIndex(
                name: "IX_Orders_PaymentCode",
                table: "Orders",
                column: "PaymentCode",
                unique: true);

            migrationBuilder.CreateIndex(
                name: "IX_Orders_SepayTransactionId",
                table: "Orders",
                column: "SepayTransactionId",
                unique: true);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropIndex(
                name: "IX_Orders_PaymentCode",
                table: "Orders");

            migrationBuilder.DropIndex(
                name: "IX_Orders_SepayTransactionId",
                table: "Orders");

            migrationBuilder.DropColumn(
                name: "PaidAt",
                table: "Orders");

            migrationBuilder.DropColumn(
                name: "PaymentCode",
                table: "Orders");

            migrationBuilder.DropColumn(
                name: "PaymentStatus",
                table: "Orders");

            migrationBuilder.DropColumn(
                name: "SepayReferenceCode",
                table: "Orders");

            migrationBuilder.DropColumn(
                name: "SepayTransactionId",
                table: "Orders");
        }
    }
}
