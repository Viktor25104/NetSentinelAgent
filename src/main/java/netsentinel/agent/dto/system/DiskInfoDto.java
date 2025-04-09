package netsentinel.agent.dto.system;

/**
 * DTO, представляющий информацию о логическом диске.
 * Используется для мониторинга состояния файловых систем.
 *
 * @param name имя диска или раздела
 * @param mount точка монтирования (например, / или C:\)
 * @param type тип файловой системы (например, NTFS, ext4)
 * @param totalBytes общий объём в байтах
 * @param usedBytes занято в байтах
 * @param freeBytes свободно в байтах
 * @param usedPercent уровень заполненности диска (0.0–100.0)
 *
 * @author Viktor Marymorych
 * @since 1.0
 */
public record DiskInfoDto(
        String name,
        String mount,
        String type,
        long totalBytes,
        long usedBytes,
        long freeBytes,
        double usedPercent
) {}
