typealias WatchlistName = String
typealias Watchlist = MutableList<String>

object Settings {
    var watchlists = mutableMapOf<WatchlistName, Watchlist>()
}