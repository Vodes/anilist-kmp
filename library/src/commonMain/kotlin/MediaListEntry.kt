package pw.vodes.anilistkmp

import pw.vodes.anilistkmp.graphql.fragment.BasicMediaListEntry
import pw.vodes.anilistkmp.graphql.fragment.MediaSmall

data class MediaListEntry(val listEntry: BasicMediaListEntry, val media: MediaSmall)