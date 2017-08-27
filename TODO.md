* Album grid: the album year is not displayed if the album name is too long (overlap)
* Add a placeholder image to use for albums and artists for which the art
  couldn't be/wasn't fecthed
* Settings fragment
  * Edit directory list (separate Activity)
  * Number of columns in the album / artist fragments
    * This number should be configurable from the toolbar as well
* Add parameters to Album list and Song list
  * Click album -> show only songs in that album
  * Click artist -> show only albums by that artist
  * Merge into a "Search" feature that also shows a limited selection
* "The Zombies" should be sorted at "Z" (not "T")
* Pick album art Activity
  * Let the user choose from:
    * Musicbrainz method 1: default search using album name and artist name
    * Musicbrainz method 2: search artist ID, then search using album name and artist ID
      * Preferred method for: "High Life" by "Brian Eno/Karl Hyde"
    * Discogs method
    * Custom file (JPG/PNG/...) selection
* Pause the music in case the headphones get unplugged

Clean code
--
* Retrofit the image fetchers (not urgent, code is quite clean)

Possible features
--
* Trim sections between `[` and `]` from artist and/or album names?
  * Don't trim ending "Disc 1" strings since it's valuable for picking an album to play

File scanning features
--
* SongsFragment should listen to album updates so that missing album art
  can be updated in the list after it is fetched
* FileScannerService should start broadcasting sooner, not after ALL
  records have been inserted

Fetching issues for album art
--
* "Go Tell Fire to the Mountain" by "WU LYF" --> 404 from coverartarchive
