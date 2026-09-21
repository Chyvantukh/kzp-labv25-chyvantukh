# Звіт до лабораторної роботи № 1

## 1. Тема, номер і варіант
- Тема: Обробка даних про товари у CSV-файлі.
- Предметна область: торгівля та облік товарів у складі/магазині.
- Номер варіанта: 25.
- Ваша операційна система: Windows 10.
- Репозиторій: https://github.com/Chyvantukh/kzp-labv25-chyvantukh

## 2. Мета роботи
Метою роботи було створити консольний Java-додаток, який зчитує дані з CSV-файлу, перевіряє їхню коректність, проводить обчислення за заданими показниками та формує звіт у текстовому файлі та в консольному виводі. У результаті досягнуто повну реалізацію процесу від читання даних до формування звіту, а також перевірки на рівні модульних і інтеграційних тестів.

## 3. Постановка задачі
Потрібно реалізувати програму, яка працює з набором даних про товари. Формат запису має такий вигляд:

Назва;Тип;Вага;Собівартість;Ціна

Приклад:
Хлiб пшеничний;хлiб;500;22.00;38.00
Печиво;печиво;200;30.00;58.00

Для варіанта № 25 необхідно обчислювати чотири показники:
1. загальну вагу всіх коректних товарів;
2. середній маржинальний прибуток;
3. найдорожчий товар;
4. перелік некоректних рядків із поясненням помилок.

Також передбачено:
- валідацію відсутніх або зайвих полів;
- перевірку числових значень;
- обробку від’ємних величин;
- формування звітного файлу у форматі UTF-8.

## 4. Структура програми
Проєкт має наступну структуру:

- src/main/java/lab/Main.java — основний клас програми;
- src/test/java/lab/MainTest.java — модульні тести;
- src/test/java/lab/MainIntegrationTest.java — інтеграційні тести;
- data/input.csv — вхідні дані;
- data/report.txt — згенерований звіт;
- pom.xml — конфігурація Maven;
- README.md — опис проекту;
- REPORT.md — звіт лабораторної роботи.

Пакет:
- lab

Потік даних:
1. CSV-файл читається з data/input.csv.
2. Кожен рядок передається до методу parseLine.
3. Метод перевіряє правильність кількості полів і типів значень.
4. Коректні записи формують список Product.
5. Обчислюються агреговані параметри.
6. Результат виводиться в консоль і записується у data/report.txt.

## 5. Інфраструктура
Проект побудовано за допомогою Maven Wrapper.
- Maven Wrapper: mvnw / mvnw.cmd
- Java: 21
- Пакування: jar
- Main class: lab.Main
- Залежності:
  - JUnit 5 для тестування;
  - SpotBugs для статичного аналізу коду;
  - maven-compiler-plugin для компіляції;
  - maven-shade-plugin для створення виконуваного JAR.
- Аналізатор: SpotBugs у фазі verify.
- Команди збірки:
  - ./mvnw verify
  - ./mvnw test
  - ./mvnw package

Для рівня 3:
- Артефакт JAR: target/kzp-labv25-chyvantukh-1.0.0.jar
- Версія: 1.0.0
- Git-тег: v1.0.0
- GitHub Actions: https://github.com/Chyvantukh/kzp-labv25-chyvantukh/actions
- GitHub Releases: https://github.com/Chyvantukh/kzp-labv25-chyvantukh/releases

### Результати запуску на трьох ОС
| ОС | Команда запуску | Результат |
|---|---|---|
| Windows 10 | java -jar target/kzp-labv25-chyvantukh-1.0.0.jar | звіт сформовано успішно |
| Ubuntu 22.04 | java -jar target/kzp-labv25-chyvantukh-1.0.0.jar | звіт сформовано успішно |
| macOS 14 | java -jar target/kzp-labv25-chyvantukh-1.0.0.jar | звіт сформовано успішно |


## 6. GitHub Issues і коміти
Завдання лабораторної роботи відстежувалися за допомогою GitHub Issues. Нижче наведено всі Issues, що стосуються реалізації та документування лабораторної роботи.

| Issue | Назва задачі | Посилання |
|---|---|---|
| #1 | feature: set up laboratory project structure | [Issue #1](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/issues/1) |
| #2 | build: configure Maven toolchain and executable JAR | [Issue #2](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/issues/2) |
| #3 | ci: add GitHub Actions matrix and artifact publishing | [Issue #3](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/issues/3) |
| #4 | feat: add basic Main class | [Issue #4](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/issues/4) |
| #5 | fix: validate local build and resolve failing checks | [Issue #5](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/issues/5) |
| #6 | chore: add editorconfig and gitattributes for cross-platform consistency | [Issue #6](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/issues/6) |
| #7 | feat: add sample CSV input with valid and invalid records | [Issue #7](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/issues/7) |
| #8 | feat: implement CSV file reading | [Issue #8](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/issues/8) |
| #9 | feat: validate CSV fields | [Issue #9](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/issues/9) |
| #10 | feat: convert numeric CSV values | [Issue #10](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/issues/10) |
| #11 | feat: calculate variant metrics | [Issue #11](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/issues/11) |
| #12 | feat: generate and output unified report | [Issue #12](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/issues/12) |
| #13 | refactor: remove unused product output method | [Issue #13](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/issues/13) |
| #14 | bug: reject non-finite numeric values | [Issue #14](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/issues/14) |
| #15 | test: add level 3 edge case tests | [Issue #15](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/issues/15) |
| #16 | bug: add JUnit dependency for parameterized tests | [Issue #16](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/issues/16) |
| #17 | feature: complete test coverage for all scenarios | [Issue #17](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/issues/17) |
| #18 | bug: correct expected values in unit tests | [Issue #18](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/issues/18) |
| #19 | docs: complete README, REPORT and Javadoc | [Issue #19](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/issues/19) |
| #20 | bug: set lab.Main as the executable JAR main class | [Issue #20](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/issues/20) |
| #21 | feature: implement --version command | [Issue #21](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/issues/21) |
| #26 | test: add integration tests for the complete application workflow | [Issue #26](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/issues/26) |
| #27 | test: add tests for boundary and invalid input cases | [Issue #27](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/issues/27) |
| #29 | docs: Finalize REPORT.md | [Issue #29](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/issues/29) |

Відповідні зміни можна переглянути серед [комітів гілки `lab01`](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/commits/lab01/).

## 7. Приклади роботи
### Вхідні дані
Файл data/input.csv містить записи:
Хлiб пшеничний;хлiб;500;22.00;38.00
Круасан;кондитерський;90;помилка;55.00
Багет;хлiб;300;;22.50
Пирiг яблучний;пирiг;-750;110.00;220.00
Печиво;печиво;200;30.00;58.00
Булочка з корицею;випiчка;120;18.50;32.00;13.25

### Консольний вивід
=== УСПiШНО ЗАВАНТАЖЕНi ТОВАРИ (2) ===
Назва = Хлiб пшеничний  | Тип = хлiб       | Вага =  500г | Собiвартiсть =  22.00 | Цiна =  38.00
Назва = Печиво          | Тип = печиво     | Вага =  200г | Собiвартiсть =  30.00 | Цiна =  58.00

=== ПОМИЛКОВi ТОВАРИ (4) ===
Рядок 2: поле "Собiвартiсть" має нечислове значення
Рядок 3: вiдсутнє поле "Собiвартiсть"
Рядок 4: поле "Вага" не може бути вiд'ємним
Рядок 6: забагато полiв (очiкувалося 5, знайдено 6)

=== Предметний обрахунок ===
Загальна вага: 700 г
Середнiй маржинальний прибуток: 22.00 грн
Найдорожчий товар: Печиво (58.00 грн)

### Файл звіту
Файл data/report.txt містить той самий вміст, що й консольний вивід, але у текстовому форматі для подальшого збереження та документування.

### Повідомлення про помилки
У разі відсутності файлу програма виводить повідомлення:
Файл не знайдено за шляхом: .../data/input.csv
Також попереджається про помилки читання:
Помилка читання файлу: ...

## 8. Тестування
Було виконано такі команди:
- ./mvnw test
- ./mvnw verify
- ./mvnw package

Результати:
- модульні тести успішно пройдено;
- інтеграційні тести успішно пройдено;
- перевірка SpotBugs без критичних помилок;
- зібрано JAR-артефакт у директорії target.

Посилання на GitHub Actions:
- https://github.com/Chyvantukh/kzp-labv25-chyvantukh/actions

## 9. Документація
У коді використано Javadoc для документування ключових класів і методів. Зокрема:

- class Main — описує призначення програми та порядок виконання;
- main(String[] args) — описує запуск і основний потік обробки;
- parseLine(String line, int lineNumber, List<String> errorLogs) — описує вхідний рядок і валідацію;
- hasAllFields(String[] fields, int lineNumber, List<String> errorLogs) — перевірка наявності та кількості полів;
- calculateTotalWeight(List<Product> products) — підрахунок загальної ваги;
- calculateAverageMargin(List<Product> products) — обчислення середнього маржинального прибутку;
- findMostExpensiveProduct(List<Product> products) — пошук найдорожчого товару;
- Product.toString() — строкове представлення товару.

## 10. Академічна доброчесність
Під час виконання лабораторної роботи використовувався інструмент GitHub Copilot / VS Code AI-асистент. Його роль полягала не лише в оформленні звіту, а й у допомозі з організацією проєкту, формуванням структури файлів, рекомендаціями щодо налаштування Maven, підбором бібліотек, перевіркою стилю Java-коду, формулюванням Javadoc, а також у підготовці та коригуванні тестів. ШІ також допомагав оцінити, які сценарії перевірки варто додати для валідації CSV-рядків, обробки помилок і перевірки результатів програми.

Я використовував рекомендації ШІ як допоміжний інструмент, а не як заміну власного розуміння задачі. Основна логіка програми, реалізація парсера CSV, обчислень і формування звіту були розроблені самостійно. Я сам перевіряв, які кроки потрібні для коректної роботи застосунку, відповідав за актуальність коду і виправляв помилки, які виявлялися під час тестування.

Під час роботи я ставив уточнюючі запитання щодо змісту звіту, структури документації, способів організації коду та способів підвищення якості тестів. Поради були використані як рекомендації, але остаточне рішення щодо тексту, коду та пояснень приймалося мною після власної перевірки.

Таким чином, ШІ допоміг з організацією, формуванням тестів і налаштуванням супровідних файлів, але основний внесок у розробку, перевірку і розуміння реалізації належить мені. Я підтверджую, що весь код і логічні рішення, що мають значення для виконання лабораторної роботи, були продумані, перевірені та прийняті мною самостійно.

## 11. Відповіді на контрольні питання
1. Що таке CSV і чому його обробка важлива?
CSV — це текстовий формат для зберігання табличних даних, в якому рядки розділяються переносами рядків, а поля в цій роботі — крапкою з комою. Він потрібен для зберігання товарів і подальшої автоматичної обробки без бази даних.

2. Чому потрібно перевіряти дані перед обчисленням?
Бо вхідні дані можуть бути неповними або некоректними: відсутні поля, від’ємні значення, текст замість чисел. Без перевірки програмний код видасть помилки або невірні підсумки.

3. Яка різниця між виводом у консоль і файлом звіту?
Консольний вивід призначений для швидкого перегляду результату, а файл report.txt зберігає звіт для документування, аналізу й подальшого використання.

4. Чому важливо використовувати UTF-8?
UTF-8 підтримує українські літери й інші символи, тому вхідні дані та результати коректно відображаються в середовищі Windows, Linux і macOS.

5. Для чого потрібен параметр --version?
Цей параметр дозволяє швидко перевірити версію програми без запуску повного аналізу даних. Це зручно для автоматичного тестування та контролю релізів.

6. Чому доцільно використовувати JUnit 5?
JUnit 5 дає змогу автоматизувати перевірку коректності роботи, робити параметризовані тести і забезпечувати стабільність системи під час змін у коді.

## 12. Висновки
У межах лабораторної роботи створено консольний Java-додаток для обробки даних про товари з CSV-файлу. Програма здійснює валідацію записів, формує звіт, обчислює запитані показники та записує результат у файл. Крім того, налаштовано Maven, JUnit 5, SpotBugs і JAR-пакування, що дає повноцінну основу для подальшої розробки.

Цей проєкт стане основою лабораторної роботи № 2, де передбачається розширення функціональності: додавання більш складної логіки обробки даних, покращення архітектури, можливість інтеграції з іншими входами/виводами, а також підготовка до більш автоматизованої складної системи обробки інформації.
