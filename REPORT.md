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
| Windows 10 | java -jar target/kzp-labv25-chyvantukh-1.0.0.jar --input data/input.csv --output data/report.txt | звіт сформовано успішно |
| Ubuntu 22.04 | java -jar target/kzp-labv25-chyvantukh-1.0.0.jar --input data/input.csv --output data/report.txt | звіт сформовано успішно |
| macOS 14 | java -jar target/kzp-labv25-chyvantukh-1.0.0.jar --input data/input.csv --output data/report.txt | звіт сформовано успішно |


## 6. GitHub Issues і коміти
Завдання лабораторної роботи відстежувалися за допомогою GitHub Issues. Нижче наведено всі Issues, що стосуються реалізації та документування лабораторної роботи.

| Issue | Мітки | Назва задачі | Посилання |
|---|---|---|---|
| #1 | `feature` | feature: set up laboratory project structure | [Issue #1](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/issues/1) |
| #2 | `build` | build: configure Maven toolchain and executable JAR | [Issue #2](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/issues/2) |
| #3 | `ci` | ci: add GitHub Actions matrix and artifact publishing | [Issue #3](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/issues/3) |
| #4 | `feat` | feat: add basic Main class | [Issue #4](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/issues/4) |
| #5 | `fix` | fix: validate local build and resolve failing checks | [Issue #5](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/issues/5) |
| #6 | `chore` | chore: add editorconfig and gitattributes for cross-platform consistency | [Issue #6](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/issues/6) |
| #7 | `feat` | feat: add sample CSV input with valid and invalid records | [Issue #7](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/issues/7) |
| #8 | `feat` | feat: implement CSV file reading | [Issue #8](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/issues/8) |
| #9 | `feat` | feat: validate CSV fields | [Issue #9](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/issues/9) |
| #10 | `feat` | feat: convert numeric CSV values | [Issue #10](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/issues/10) |
| #11 | `feat` | feat: calculate variant metrics | [Issue #11](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/issues/11) |
| #12 | `feat` | feat: generate and output unified report | [Issue #12](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/issues/12) |
| #13 | `refactor` | refactor: remove unused product output method | [Issue #13](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/issues/13) |
| #14 | `bug` | bug: reject non-finite numeric values | [Issue #14](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/issues/14) |
| #15 | `test` | test: add level 3 edge case tests | [Issue #15](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/issues/15) |
| #16 | `bug` | bug: add JUnit dependency for parameterized tests | [Issue #16](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/issues/16) |
| #17 | `feature` | feature: complete test coverage for all scenarios | [Issue #17](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/issues/17) |
| #18 | `bug` | bug: correct expected values in unit tests | [Issue #18](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/issues/18) |
| #19 | `docs` | docs: complete README, REPORT and Javadoc | [Issue #19](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/issues/19) |
| #20 | `bug` | bug: set lab.Main as the executable JAR main class | [Issue #20](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/issues/20) |
| #21 | `feature` | feature: implement --version command | [Issue #21](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/issues/21) |
| #26 | `test` | test: add integration tests for the complete application workflow | [Issue #26](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/issues/26) |
| #27 | `test` | test: add tests for boundary and invalid input cases | [Issue #27](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/issues/27) |
| #29 | `docs` | docs: Finalize REPORT.md | [Issue #29](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/issues/29) |
| #30 | `feature` | feature: changing the delimiter for the split() function | [Issue #30](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/issues/30) |
| #31 | `feature` | feature: add the --help option to display help and instructions on how to run the programme | [Issue #31](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/issues/31) |
| #32 | `docs` | docs: add the answers to all the assessment questions for the laboratory session to REPORT.md | [Issue #32](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/issues/32) |
| #33 | `feature` | feature: terminate the programme using System.exit() with the appropriate exit code | [Issue #33](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/issues/33) |
| #34 | `feature` | feature: report empty input lines as validation errors | [Issue #34](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/issues/34) |
| #35 | `documentation` | docs: update REPORT.md with all implemented issues | [Issue #35](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/issues/35) |
| #37 | `feature` | feature: add mandatory input and output command-line arguments | [Issue #37](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/issues/37) |
| #38 | `documentation` | docs: update README.md and REPORT.md for the final project changes | [Issue #38](https://github.com/Chyvantukh/kzp-labv25-chyvantukh/issues/38) |

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
1. Яке призначення файла pom.xml?
Це головний конфігураційний файл у Maven-проєкті. У ньому я вказував версію Java (21), підключав бібліотеку JUnit 5, плагіни для SpotBugs та створення виконувального .jar. Завдяки pom.xml проєкт збирається однаково на будь-якому комп'ютері та операційній системі.

2. Чим відрізняються фази test, verify і package у Maven?
Команда test запускає мої JUnit-тести для перевірки поведінки коду. package проганяє тести й збирає готовий виконувальний .jar-файл у папці target. Фаза verify робить усе те саме, але ще й запускає статичний аналізатор SpotBugs для додаткової перевірки якості коду.

3. Навіщо потрібен Maven Wrapper?
Він потрібен для того, щоб проєкт можна було зібрати навіть на комп'ютері, де не встановлено Maven. Скрипти mvnw та mvnw.cmd самі завантажують потрібну версію збирача під час першого запуску. Це гарантує, що у всіх розробників і на GitHub Actions буде використовуватися однакова версія.

4. Яку роль відіграє метод main?
Це точка входу в програму, з якої JVM починає її виконання. У моєму коді main приймає аргументи командного рядка (наприклад, --version або --help), викликає зчитування CSV-файлу, запускає валідацію, рахує підсумкові показники та виводить результат у консоль і файл.

5. Чим примітивний тип відрізняється від String?
Примітиви (як int чи double) зберігають прості значення безпосередньо і не є об'єктами. String — це клас для роботи з текстом, який має власні методи на зразок split() чи trim(). Оскільки з файлу ми читаємо все як текст, то спочатку отримуємо String, а вже потім явно перетворюємо його на числа.

6. Чому середнє потрібно обчислювати в double, навіть якщо сума й кількість мають тип int?
Якщо ділити один int на інший, Java відкине дробову частину і поверне ціле число (наприклад, 5 / 2 буде 2, а не 2.5). Щоб зберегти точність при розрахунку середнього прибутку, треба привести значення до double ще до проведення операції ділення.

7. Що станеться, якщо Integer.parseInt отримає рядок "abc"?
Метод викине помилку NumberFormatException, бо текст "abc" неможливо перетворити на число. Якщо її не перехопити через try-catch, програма просто впаде. Тому я ловлю цей виняток і виводжу зрозуміле повідомлення про помилку з номером рядка.

8. Чому не можна мовчки пропускати рядок із помилкою?
Якщо мовчки пропустити некоректний рядок, користувач не дізнається, що частина даних втратилася, а підсумкові обчислення будуть неправильними. Важливо відфільтрувати некоректний рядок, але обов'язково зафіксувати у звіті, який саме рядок було пропущено і з якої причини.

9. Як String.split(";", -1) обробляє порожнє останнє поле?
Параметр -1 вказує методу split не видаляти порожні елементи наприкінці рядка. Якщо в кінці запису буде порожнє поле, з цим параметром воно повернеться як порожній String, а не просто відкинеться. Це дозволяє програмі помітити, що в рядку бракує обов'язкових даних.

10. Чому для шляхів краще використовувати Path.of, а не "data\\input.csv"?
Рядок "data\\input.csv" буде нормально працювати тільки на Windows, бо на Linux та macOS використовується прямий слеш /. Path.of("data", "input.csv") сам підбирає правильний роздільник залежно від операційної системи, що робить код кросплатформним.

11. Навіщо явно задавати StandardCharsets.UTF_8?
За замовчуванням різні ОС використовують власні системні кодування, через що українські літери при зчитуванні можуть перетворитися на незрозумілі символи. Явне вказання StandardCharsets.UTF_8 гарантує, що файл буде однаково прочитано та записано на будь-якій платформі.

12. Чим %d відрізняється від %n у форматованому виводі?
Специфікатор %d підставляє ціле число у форматований рядок (наприклад, кількість товарів чи номер рядка). А %n переносить текст на новий рядок, причому використовує роздільник, прийнятий у поточної операційної системи.

13. Які дані потрібно перевірити у вашому варіанті (варіант 25 — товари, вага, собівартість, ціна)?
Я перевіряю правильну кількість полів у рядку, відсутність порожніх значень та можливість перетворити вагу, собівартість і ціну на числа. Також контролюю, щоб ціна та вага не були від'ємними або некоректними. До обчислень доходять тільки ті товари, які пройшли всі ці перевірки.

14. Які тести виявлять помилку в обчисленні середнього?
Тест, де сума не ділиться націло на кількість (наприклад, сума 10 і кількість 3), одразу покаже помилку цілочисельного ділення. Також важливо протестувати випадок з одним товаром, відсутність валідних записів і ситуацію, коли у вхідному файлі є некоректні рядки.

15. Яке призначення статичного аналізатора SpotBugs?
SpotBugs аналізує скомпільований байт-код і шукає потенційні баги, витоки пам'яті чи погані практики програмування без запуску самої програми. Він працює під час фази verify і виступає додатковим рівнем контролю якості коду.

16. Яку роль виконує GitHub Actions?
Це сервіс автоматизації (CI/CD), який при кожному коміті чи Pull Request заново збирає проєкт і запускає тести. У моїй роботі він автоматично перевіряє збірку одразу на трьох ОС — Windows, Ubuntu та macOS, підтверджуючи кросплатформність.

17. Що має містити GitHub Issue на дефект?
Зрозумілий заголовок, опис проблеми, кроки для відтворення помилки, а також очікуваний і фактичний результати. Також корисно вказувати приклад некоректного рядка і пов'язувати Issue з комітом чи Pull Request, де зроблено виправлення.

18. Що потрібно вказати у розділі звіту про академічну доброчесність?
Потрібно чесно описати, чи використовувався ШІ, які саме інструменти залучалися (наприклад, GitHub Copilot) і яка була їхня роль. Важливо зазначити, що штучний інтелект виконував лише роль консультанта, а весь код і підсумкову логіку я перевірив і зрозумів особисто.

19. Яку роботу виконує агент DevOps, а яку агент Валідатор?
Агент DevOps займається інфраструктурою: налаштуванням pom.xml, Maven Wrapper, збіркою JAR та роботою GitHub Actions. А агент Валідатор перевіряє саму програму: шукає крайові випадки, складає тести для перевірки некоректних даних та формує описи дефектів.

20. Який рядок вашої програми найскладніше пояснити і чому?
Найскладнішим є рядок із викликом line.split(...) у поєднанні з наступною перевіркою полів та перехопленням NumberFormatException. Тут в одному місці перетинаються правила розділення тексту, логіка валідації CSV та обробка винятків при перетворенні типів даних.

## 12. Висновки
У межах лабораторної роботи створено консольний Java-додаток для обробки даних про товари з CSV-файлу. Програма здійснює валідацію записів, формує звіт, обчислює запитані показники та записує результат у файл. Крім того, налаштовано Maven, JUnit 5, SpotBugs і JAR-пакування, що дає повноцінну основу для подальшої розробки.

Цей проєкт стане основою лабораторної роботи № 2, де передбачається розширення функціональності: додавання більш складної логіки обробки даних, покращення архітектури, можливість інтеграції з іншими входами/виводами, а також підготовка до більш автоматизованої складної системи обробки інформації.
