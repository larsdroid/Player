* AudioFileReader: 33, 47,48, 55
  Complete overhaul: insert album, artist and song in the file reader
  --> broadcast!

* AlbumsFragment/AlbumRecyclerViewAdapter pair is implemented differently from
  ArtistsFragment/ArtistRecyclerViewAdapter. Also check SongsFragment/SongRecyclerViewAdapter
* Calculate album length and show it in the GUI
  * Full scan of an MP3 file should be done in a separate thread
    followed by a new type of broadcast: 'SONG_UPDATED'
* Application state: save previous track position millis
  * Save current track and current millis **per album**
  * Save album **play count** per album
  * *Current Song* and *current millis* should no longer be stored **globally**,
    only *current album*
* When the Activity is terminated, pressing the notification should open the acticity
 **in album view with the correct album selected and scrolled to the correct song**
* Add a placeholder image to use for albums and artists for which the art
  couldn't be/wasn't fecthed
* Album Fragment: toolbar should have some back button of a quick way to
  get back to the music fragment
* Pick album art Activity
  * Let the user choose from:
    * Musicbrainz method 1: default search using album name and artist name
    * Musicbrainz method 2: search artist ID, then search using album name and artist ID
      * Preferred method for: "High Life" by "Brian Eno/Karl Hyde"
    * Discogs method
    * Custom file (JPG/PNG/...) selection
* "The Zombies" should be sorted at "Z" (not "T")
* Clean up/remove all unused icons

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
  * Replace 'FileScannerService::insertRecords' with 'FileScannerService::insertRecord'
* PurgingService

Fetching issues for album art
--
* "Go Tell Fire to the Mountain" by "WU LYF" --> 404 from coverartarchive

Post-release
--
* Settings fragment
  * Number of columns in the album / artist fragments
    * This number should be configurable from the toolbar as well

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
