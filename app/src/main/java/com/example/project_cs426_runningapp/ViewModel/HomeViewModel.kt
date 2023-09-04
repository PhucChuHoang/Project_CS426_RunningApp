package com.example.project_cs426_runningapp.ViewModel

class HomeViewModel {

    companion object {
        public var instance = ""

        @JvmStatic fun get(): String {
            return instance;
        }
        @JvmStatic fun set(name: String) {
            instance = name;
        }
    }

}