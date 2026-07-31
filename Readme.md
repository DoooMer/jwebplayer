[Docker image](https://hub.docker.com/r/doomer/jwebplayer)

Just audio player for your music collection worked in browser.

Do not recommend to use it in public server.


## How it works

Application scans directory with mp3's you chosen. Write file's data, such as filename and directory path, in local database. 
Then makes default playlist with name 'default'. That is it.

Application watches changes in directory and add a new files to default playlist.


## Get started

You have to set up some properties:
- `USER_DEFAULT_PASSWORD` (ENV) should be just some stupid string, like 'password' (especially if app runs in local network)
etc.


Write them somewhere.


Pull image from docker hub
```bash
docker pull doomer/jwebplayer
```


Then run container like this
```bash
docker run -p 8080:8080 -e USER_DEFAULT_PASSWORD=password doomer/jwebplayer
```


So, now you be able to open `http://localhost:8080` and listen to the music.