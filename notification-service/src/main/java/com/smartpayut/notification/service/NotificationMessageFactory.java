package com.smartpayut.notification.service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

import org.springframework.stereotype.Component;

import com.smartpayut.notification.domain.enumeration.NotificationType;
import com.smartpayut.notification.event.PaymentEvent;
import com.smartpayut.notification.event.WalletEvent;

@Component
public class NotificationMessageFactory {

    public MessageContent forWallet(WalletEvent event) {
        return switch (event.eventType()) {
            case "wallet.created" -> content("Billetera creada", "Tu billetera fue creada correctamente.");
            case "wallet.credited" -> content(
                    "Fondos acreditados", "Se acreditaron fondos en tu billetera" + amount(event.amount()) + ".");
            case "wallet.debited" -> content(
                    "Débito realizado", "Se realizó un débito de tu billetera" + amount(event.amount()) + ".");
            case "wallet.refunded" -> content(
                    "Reembolso acreditado", "Se realizó un reembolso a tu billetera" + amount(event.amount()) + ".");
            default -> throw new IllegalArgumentException("Evento Wallet no soportado: " + event.eventType());
        };
    }

    public MessageContent forPayment(PaymentEvent event) {
        String method = event.method() == null || event.method().isBlank()
                ? "" : " mediante " + event.method();
        return switch (event.eventType()) {
            case "payment.completed" -> content(
                    "Pago completado", "Tu pago" + method + amount(event.amount()) + " fue procesado correctamente.");
            case "payment.failed" -> content(
                    "Pago no completado", "No fue posible completar tu pago" + method + amount(event.amount()) + ".");
            case "payment.refunded" -> content(
                    "Pago reembolsado", "Tu pago" + amount(event.amount()) + " fue reembolsado.");
            case "topup.completed" -> content(
                    "Recarga completada", "Tu recarga" + amount(event.amount()) + " fue acreditada correctamente.");
            case "topup.failed" -> content(
                    "Recarga no completada", "No fue posible completar tu recarga" + amount(event.amount()) + ".");
            default -> throw new IllegalArgumentException("Evento Payment no soportado: " + event.eventType());
        };
    }

    public NotificationType type(String eventType) {
        return NotificationType.valueOf(eventType.replace('.', '_').toUpperCase(Locale.ROOT));
    }

    private String amount(BigDecimal value) {
        if (value == null) {
            return "";
        }
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
        return " por " + format.format(value);
    }

    private MessageContent content(String title, String message) {
        return new MessageContent(title, message);
    }

    public record MessageContent(String title, String message) {
    }
}
