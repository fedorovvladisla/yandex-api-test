# Yandex Disk API Автотесты

Проект с автотестами для проверки REST API Яндекс.Диска (https://yandex.ru/dev/disk/rest/).  
Тесты написаны на **Java 17** с использованием **JUnit 5**, **Rest Assured**.  
Обеспечено покрытие методов **GET, POST, PUT, DELETE, PATCH** для ключевых эндпоинтов.

---

## Технологии

- Java 17
- Maven
- JUnit 5
- Rest Assured
- AssertJ / JUnit Assertions
- SLF4J (логирование)
---

## Настройка и запуск

### 1. Получение OAuth-токена

Для работы тестов нужен OAuth-токен тестового аккаунта Яндекс.Диска (не личного!).  
Инструкция: [https://yandex.ru/dev/disk/doc/concepts/quickstart-docpage/](https://yandex.ru/dev/disk/doc/concepts/quickstart-docpage/)
---
### 2. Переменная окружения

Установите переменную окружения `YA_DISK_TOKEN` с полученным токеном.

**Windows (cmd):**
```cmd
set YA_DISK_TOKEN=ваш_токен
```
**Windows (PowerShell):**

```powershell
$env:YA_DISK_TOKEN="ваш_токен"
```
**macOS / Linux:**

```bash
export YA_DISK_TOKEN=ваш_токен
```
---
### 3. Запуск тестов
```
mvn clean test
```
---

### 4. Покрытие эндпоинтов

| Метод     | Эндпоинт                                      | Что проверяет тест                          |
|-----------|-----------------------------------------------|---------------------------------------------|
| **GET**   | `/v1/disk`                                    | Информация о диске (user, total_space)      |
| **GET**   | `/v1/disk/resources`                          | Метаданные папки/файла                      |
| **GET**   | `/v1/disk/resources/files`                    | Плоский список файлов (пагинация, сортировка, фильтр по типу) |
| **GET**   | `/v1/disk/resources/last-uploaded`            | Список недавно загруженных файлов           |
| **GET**   | `/v1/disk/resources/upload`                   | Получение URL для загрузки файла            |
| **GET**   | `/v1/disk/trash/resources`                    | Содержимое корзины                          |
| **GET**   | `/v1/disk/public/resources`                   | Метаинформация публичного ресурса           |
| **PUT**   | `/v1/disk/resources`                          | Создание папки (и вложенной)                |
| **PUT**   | `/v1/disk/resources/upload` (ссылке)          | Загрузка файла (в т.ч. с перезаписью)       |
| **PUT**   | `/v1/disk/resources/publish`                  | Публикация папки                            |
| **PUT**   | `/v1/disk/trash/resources/restore`            | Восстановление ресурса из корзины           |
| **DELETE**| `/v1/disk/resources`                          | Удаление папки/файла (безвозвратно или в корзину) |
| **DELETE**| `/v1/disk/trash/resources`                    | Очистка корзины                             |
| **POST**  | `/v1/disk/resources/copy`                     | Копирование файла                           |
| **POST**  | `/v1/disk/resources/move`                     | Перемещение файла                           |
| **PATCH** | `/v1/disk/resources`                          | Добавление / обновление кастомных свойств   |

---
 ### Дополнительно
- Все тесты создают временные ресурсы и автоматически удаляют их после выполнения.

- Базовая папка для тестов: disk:/autotest_example, которая создаётся автоматически при первом запуске.