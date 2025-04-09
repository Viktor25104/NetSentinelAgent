package netsentinel.agent.dto.system;

/**
 * DTO, содержащий информацию о процессоре (CPU).
 * Используется как часть мониторинга загрузки и конфигурации системы.
 *
 * @param name имя/модель процессора
 * @param physicalCores количество физических ядер
 * @param logicalCores количество логических (виртуальных) ядер
 * @param load загрузка процессора в процентах (0.0–100.0)
 *
 * @author Viktor Marymorych
 * @since 1.0
 */
public record CpuInfoDto(
        String name,
        int physicalCores,
        int logicalCores,
        double load
) {}
