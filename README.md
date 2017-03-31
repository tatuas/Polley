[![CircleCI](https://circleci.com/gh/tatuas/polley.svg?style=svg&circle-token=335f90b71cbf97a7d60efc33ac3712d8e8678d2f)](https://circleci.com/gh/tatuas/polley)

[![MIT License](http://img.shields.io/badge/license-MIT-blue.svg?style=flat)](LICENSE)
[![Now Deprecated](https://img.shields.io/badge/now-Deprecated-red.svg)](README.md)

# Polley [Deprecated]

Android large ***P*** ayload POST library, respect V ***olley*** .

***Polley*** can safely execute multi thread Large payload POST.

**Polley is now DEPRECATED. Use okhttp when large payload POST.**

# How to install

${YOUR_APP_MODULE}/build.gradle
```
repositories {
    maven { url 'http://tatuas.github.io/polley/repository/' }
}

dependencies {
    compile 'com.tatuas.android:polley:0.0.1@aar'
}
```

# How to build library aar

```bash
$ git clone https://github.com/tatuas/polley.git

// build in Android Studio

$ ./gradlew library:clean library:assembleRelease library:uploadArchives

// Copy "repository/" via Finder

$ git checkout gh-pages

// Paste "repository/" and commit

$ git push origin gh-pages
```
