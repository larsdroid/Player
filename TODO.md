* InfoSource attribute should be moved from AbstractImage to AbstractAlbum
  and AbstractArtist
* Add a placehold image to use for albums and artists for which the art
  couldn't be/wasn't fecthed

Clean code
--
* Retrofit the image fetchers (not urgent, code is quite clean)

Bugs
--
* AbstractAlbum: the artist field should be eagerly fetched.  
  AbstractSong: the artist and album fields should be eagerly fetched.  
  Waiting for requery to implement eagerly fetching relations:
  https://github.com/requery/requery/issues/654  
  Alternatively, handy JOIN support when doing a SELECT would be helpful
  as well, since at the moment the amount of queries is ridiculous...
