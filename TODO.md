Album and Artist Images
--
* A new NULLABLE column for image data should be added to the artist and
  album tables. (update DAO's etc.)
* A new ImageFetchService hsould be created to fetch images for all artists
  and albums that don't have an image in the DB yet.
* MainActivity should launch ImageFetchService.
* FileScannerService should launch ImageFetchService after new data is
  insert into the DB. (and in case it's not running yet?)

Clean code
--
* Retrofit the image fetchers (not urgent, code is quite clean)
