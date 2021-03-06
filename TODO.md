Most urgent
--
* Implement 'repeat one', 'repeat all' and 'shuffle'
* When the Activity is terminated, pressing the notification should open the acticity
  **in album view with the correct album selected and scrolled to the correct song**.
  This will have to be implemented manually, there's no way to put info into the Intent
  and open the correct Fragment.
* Pick album art Activity
  * Let the user choose from:
    * Musicbrainz method 1: default search using album name and artist name
    * Musicbrainz method 2: search artist ID, then search using album name and artist ID
      * Preferred method for: "High Life" by "Brian Eno/Karl Hyde"
    * Discogs method
    * Custom file (JPG/PNG/...) selection

Less urgent
--
* Implement "times played" counter per album. The UI displays are
  already present on the `AlbumFragment`. When an album is finished
  playing, the counter should go up (`Player::onPlayerStateChanged`)
* Add a placeholder image to use for albums and artists for which the art
  couldn't be/wasn't fetched
* Album Fragment toolbar: collapsed mode:
  * Background should be primary color
  * Left margin/padding should be removed
* Clean up/remove all unused icons
* SongWithAlbumInfo -- LiveData: album image data should be a nested Object (currently copying the
  data in each song)

Notification
--
* Notification display: show progressbar instead of album cover in case
  the art wasn't fetched yet
  * Is it possible to use LiveData for the notification?

Possible features
--
* "The Zombies" should be sorted at "Z" (not "T")
* Trim sections between `[` and `]` from artist and/or album names?
  * Don't trim ending "Disc 1" strings since it's valuable for picking an album to play

File scanning features
--
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
