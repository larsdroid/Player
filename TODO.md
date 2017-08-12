Album and Artist Images
--
* https://musicbrainz.org/doc/XML_Web_Service/Rate_Limiting#How_can_I_be_a_good_citizen_and_be_smart_about_using_the_Web_Service.3F
* Check "good practices" for Discogs as well

Clean code
--
* Retrofit the image fetchers (not urgent, code is quite clean)

Bugs
--
* AbstractAlbum: the Artist field should be eagerly fetched. Waiting for
  requery to implement eagerly fetching relations:
  https://github.com/requery/requery/issues/654
