*** URGENT: PlayBack singleton shouldn't be accessed from inside
*** the service since it runs in a separate process! So some global
*** broadcast listener (a new one?) from the MAIN process should
*** update the application state.

* Start a song -> press pause -> dismiss the activity -> open the notification
  -> notification shows the previously stored "current track"

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
* Clean up/remove all unused icons

Filters
--
* SongsFragment filter:
  * The "All" (albums) and "All" artists menu checkboxes should be tri-state
* SongsFragment filter: the artist-checkboxes should be tri-state:
  * unchecking an album should put that album's artist in the "third"/"undetermined" state
  * unchecking an artist should uncheck all albums of that artist (can be done in the filter class)

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
* The scanner should not only scan directories, but also files that are "listed"
  in the Android media store (these should be done first actually)
* PurgingService

Fetching issues for album art
--
* "Go Tell Fire to the Mountain" by "WU LYF" --> 404 from coverartarchive
