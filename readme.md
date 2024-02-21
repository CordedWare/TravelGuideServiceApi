Приложениие извлекает данные из Wikidata (бесплатный сервис, предоставляющий конечную точку SPARQL). Также используется Apache Jena для подключения к серверу Wikidata и отправки ему запросов. Функциональное ядро состоит из Scala 2.13 и функциональных библиотек cats, cats-effect и fs2-core

Спроектировано функциональное ядро содержащее только чистые функции и неизменяемые значения, которые используются внешними модулями, такими как основной процесс приложения, который «настраивает» чистые функции, передавая им определенные действия ввода и вывода.

Внутри уровень доступа к данным, реализованный с помощью императивной библиотеки Apache Jena, поддерживающий собственный пул соединений и контексты выполнения запросов. Это ресурсы с состоянием, которые необходимо получать (например, для создания соединения с сервером) и освобождать, когда они больше не нужны, даже в случае появления ошибок. Если не освобождать такие ресурсы, то будут происходить их утечки. Можно с легкостью управлять такими случаями, использовав значения Resource, описывающие освобождаемые ресурсы.

Ускорено приложение с помощью кэширования. Реализовано параллельное выполнение запросов и используется кеш результатов.

В TravelGuideService задаются настройки поиска (позже планируется кастомизировать доступ к запросам и добавить UI).


Функциональные требования.

Путеводитель по поп-культуре
1. Приложение принимает строковое значение: поисковый запрос, касающийся туристической достопримечательности, которую пользователь хочет посетить и по которой ему нужен путеводитель.
2. Приложение отыскивает данную достопримечательность, ее описание (если существует) и географическое местоположение. Предпочтение отдается регионам с большей численностью населения.
3. Приложение должно использует местоположение для:
   •поиска исполнителей из этого региона, отсортированного по количеству подписчиков в социальных сетях;
   •поиска фильмов, действие которых разворачивается в этом месте, отсортированного по кассовым сборам.
4. Для данной туристической достопримечательности формируется перечень исполнителей и фильмов, образующий «путеводитель по поп-культуре», который возвращается пользователю. Если возможных путеводителей получится несколько, то приложение возвращает с наивысшим баллом. Баллы рассчитываются следующим образом:
   <br>- 30 баллов за описание;
   <br>- 10 баллов за каждого исполнителя или фильм (но не более 40 баллов);
   <br>- 1 балл за каждые 100 000 подписчиков (в сумме для всех исполнителей, но не более 15 баллов);
   <br>- 1 балл за каждые 10 000 000 долларов кассовых сборов (в сумме для всех фильмов, но не более 15 баллов).