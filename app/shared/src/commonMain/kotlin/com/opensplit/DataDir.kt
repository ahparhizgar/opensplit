package com.opensplit

data class DataDir(val dir: String) {
    companion object {
        const val DEFAULT = "app-data/default"
    }
}
