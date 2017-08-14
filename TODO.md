* Add a placeholder image to use for albums and artists for which the art
  couldn't be/wasn't fecthed
* SongsFragment should listen to album updates so that missing album art
  can be updated in the list after it is fetched
* FileScannerService should start broadcasting sooner, not after ALL
  records have been inserted
* Material Design:  
  https://material.io/guidelines/components/dividers.html#dividers-types-of-dividers  
  https://stackoverflow.com/questions/37047735/android-material-listview-dividers

Clean code
--
* Retrofit the image fetchers (not urgent, code is quite clean)

Possible features
--
* Trim sections between `[` and `]` from artist and/or album names?
