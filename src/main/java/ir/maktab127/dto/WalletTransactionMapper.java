package ir.maktab127.dto;

import ir.maktab127.entity.WalletTransaction;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class WalletTransactionMapper {
    public static WalletTransactionDto toDto(WalletTransaction tx) {
        WalletTransactionDto dto = new WalletTransactionDto();
        dto.setId(tx.getId());
        dto.setAmount(tx.getAmount());
        dto.setCreateDate(tx.getCreateDate() != null ? LocalDateTime.parse(tx.getCreateDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)) : null);
        dto.setDescription(tx.getDescription());
        return dto;
    }
}
