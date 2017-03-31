# Polley [Deprecated]

Android large ***P*** ayload POST library, respect V ***olley*** .

***Polley*** can safely execute multi thread Large payload POST.

**Polley is deprecated. Use okhttp when large payload POST.**

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
