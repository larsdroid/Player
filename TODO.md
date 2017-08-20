* Screen rotate at i.e. album list creates a second fragment on top of the previous?
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
* "Now playing" bottom bar
* "Now playing" notification bar
* Continue to next song in album after a song is over
* "The Zombies" should be sorted at "Z" (not "T")

Clean code
--
* Retrofit the image fetchers (not urgent, code is quite clean)

Possible features
--
* Trim sections between `[` and `]` from artist and/or album names?

File scanning features
--
* SongsFragment should listen to album updates so that missing album art
  can be updated in the list after it is fetched
* FileScannerService should start broadcasting sooner, not after ALL
  records have been inserted

Fetching issues for album art
--
* High Life by Brian Eno, Karl Hyde
* (What's the story) Morning Glory by Oasis
* Catch the breeze (Disc 1)
* Catch the breeze (Disc 2)
* Zombie Heaven (Disc 2) by The Zombies
* Go Tell Fire to the Mountain