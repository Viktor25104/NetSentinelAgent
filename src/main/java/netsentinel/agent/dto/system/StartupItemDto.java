package netsentinel.agent.dto.system;

/**
 * DTO, описывающее элемент автозагрузки (startup item).
 *
 * @param name имя или путь к исполняемому файлу
 * @param location источник автозагрузки (например, Startup Folder)
 * @param enabled флаг активности (true = включен, false = отключен)
 *
 * @author Viktor Marymorych
 * @since 1.0
 */
public record StartupItemDto(
        String name,
        String location,
        boolean enabled
) {}
