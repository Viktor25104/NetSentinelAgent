package netsentinel.agent.dto.system;

/**
 * DTO, представляющий текущее состояние оперативной памяти (RAM).
 *
 * @param totalBytes общий объём RAM в байтах
 * @param usedBytes объём занятой памяти
 * @param freeBytes объём свободной памяти
 * @param usedPercent загрузка RAM в процентах (0.0–100.0)
 *
 * @author Viktor Marymorych
 * @since 1.0
 */
public record RamInfoDto(
        long totalBytes,
        long usedBytes,
        long freeBytes,
        double usedPercent
) {}
