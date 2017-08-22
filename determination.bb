Graphics 1280,720,32,1
AutoMidHandle True ; Basically, images will be placed based on center, not left corner
SetBuffer BackBuffer() ; a back buffer
SeedRnd = MilliSecs() ; set up our random number generator
Global timer = MilliSecs() ; make an overall timer
Global enemyCount = 0 
Global astTimer = MilliSecs()

;load images and sound
Global loading = LoadImage("loading.bmp")
Global backgroundImageClose = LoadImage("stars.bmp")
Global backgroundImageFar = LoadImage("starsfarther.bmp")
Global asteroidImage = LoadImage("asteroid.bmp")
Global asteroidSmallImage = LoadImage("asteroidsmall.bmp")
Global playerImage = LoadImage("player.bmp")
Global bulletImage = LoadImage("bullet.bmp")
Global enemyBulletImage = LoadImage("enemybullet.bmp")
Global enemyImage = LoadImage("enemy.bmp")
Global damageSound = LoadSound("damage.wav")
Global playerShoot = LoadSound("playershoot.wav")
Global enemyShoot = LoadSound("enemyshoot.wav")

;establish our constants for controls
Const ESCKEY = 1, UPKEY = 200, LEFTKEY = 203, RIGHTKEY = 205, DOWNKEY = 208, SPACEBAR = 57


; types
Type player
	Field x,y,health
	Field isDead
	Field image 
End Type

Type bullet 
	Field x,y,dy
	Field image
End Type 

Type enemyBullet
	Field x,y,dy
	Field image
End Type 

Type enemy
	Field x,y,dx,dy,health
	Field image
	Field isDead
	Field bulletTimer 
End Type 

Type asteroid
	Field x,y,dx,dy,health
	Field image
	Field isSmall
End Type 

; make our player!
Global player.player = New player
player\x = 600
player\y = 670
player\isDead = False
player\image = playerImage

; Make our first enemy
Global enemy.Enemy = New Enemy 
enemy\x = 600
enemy\y = 50 
enemy\dy = 0 
enemy\dx = 8
enemy\isDead = False
enemy\image = enemyImage 
enemy\bulletTimer = MilliSecs()
enemyCount = enemyCount + 1 

;loading screen! Quits if we hit escape, goes on to next loop if we hit enter
While KeyDown(28)=0
	DrawImage loading,640,450
	If (KeyHit(1)) Then End 
	
	Flip 
Wend

Global scrolly = 0 

;main game loop
.mainLoop
While KeyDown(1)=0
	Cls
	If Not ChannelPlaying(music)
		music = PlayMusic("afterburner.mp3")
	EndIf 
	updateBackground() ; Parallax stuff
	Text 200,700,"Time Elapsed: " + Str((MilliSecs() - timer) / 1000)
	Text 400,700,"Enemy Count: " + Str(enemyCount)
	updateEnemy() ; Enemy movement and shooting
	updatePlayer() ; Player movement (keyboard) and shooting
	updateBullets() ; Check bullet collisions and movement
	updateEnemyBullets() ; Check enemy bullet collisions and movement
	updateAsteroids()
	Flip
Wend
StopChannel music 

Function updateBackground() ; Controls the background
	TileImage backgroundimageFar,0,scrolly ;Background
	TileImage backgroundImageClose,0,scrolly*2 ;Foreground, moves faster
	scrolly = scrolly+1 ; Scroll forward 
	If(scrolly >= ImageHeight(backgroundimageclose)) Then
		scrolly = 0 ;Reset if needed
	EndIf
End Function

Function updatePlayer()
	DrawImage player\image,player\x,player\y
	;Keyboard controls
	If KeyDown(LEFTKEY)
		player\x = player\x - 5			
	EndIf
	If KeyDown(RIGHTKEY)
		player\x = player\x + 5
	EndIf
	
	If KeyDown(UPKEY)
		player\y = player\y - 5
	EndIf
	If KeyDown(DOWNKEY)
		player\y = player\y + 5
	EndIf
	
	If KeyHit(SPACEBAR) ;If we shoot, make a new bullet
		bullet.bullet = New bullet
		bullet\image = bulletImage
		bullet\dy = -10
		bullet\x = player\x
		bullet\y = player\y
		PlaySound playerShoot
	EndIf 
	If KeyHit(18) ; Enemy respawn feature for testing
		enemy.Enemy = New Enemy 
		enemy\x = 600
		enemy\y = 50 
		enemy\dy = 0 
		enemy\dx = 8
		enemy\isDead = False
		enemy\image = enemyImage 
		enemy\bulletTimer = MilliSecs()
		enemyCount = enemyCount + 1 
	EndIf 
		
End Function 

Function updateBullets() 
	For bullet.bullet = Each bullet ;For ALL bullets in memory
		DrawImage(bullet\image,bullet\x,bullet\y)
		bullet\y = bullet\y + bullet\dy ; move it!
		If(bullet\y <= 0) Then ; Delete if out of bounds 
			Delete bullet
		Else
			For enemy.enemy = Each enemy ; Check for collisions with enemies
				If ImagesOverlap(bullet\image,bullet\x,bullet\y,enemy\image,enemy\x,enemy\y)
					Delete enemy
					enemyCount = enemyCount - 1 
					PlaySound damageSound 
				EndIf 
			Next
			For asteroid.asteroid = Each asteroid
				If ImagesOverlap(bullet\image,bullet\x,bullet\y,asteroid\image,asteroid\x,asteroid\y)
					If(Not asteroid\isSmall) Then
						spawnSmallAsteroids(asteroid\x,asteroid\y)
						spawnSmallAsteroids(asteroid\x,asteroid\y)
						spawnSmallAsteroids(asteroid\x,asteroid\y)
						spawnSmallAsteroids(asteroid\x,asteroid\y)
					EndIf 
					Delete asteroid
				EndIf 
			Next
		EndIf 
		
	Next
End Function 

Function updateEnemyBullets()
	For bullet.enemyBullet = Each enemyBullet ; For ALL enemy bullets
		DrawImage(bullet\image,bullet\x,bullet\y)
		bullet\y = bullet\y + bullet\dy
		If(bullet\y > 900) Then
			Delete bullet ; Delete if out of bounds
		Else ; Check for collisions. You'll need to modify this if you want
			; to have multiple players. 
			If(ImagesOverlap(bullet\image,bullet\x,bullet\y,player\image,player\x,player\y))
				PlaySound damageSound
				Delete bullet 
			EndIf
		EndIf 
	Next
End Function 
	
Function updateEnemy() 
	For enemy.enemy = Each enemy ;For ALL enemies in memory
		DrawImage(enemy\image,enemy\x,enemy\y)
		enemy\x = enemy\x + enemy\dx 
		enemy\y = enemy\y + enemy\dy ; probably just zero for now
		If(enemy\x <= 0 Or enemy\x >= 1280) Then enemy\dx = -enemy\dx ; bounce
		If(MilliSecs() - enemy\bulletTimer >= 2000) Then ; Once every second
			bullet.enemyBullet = New enemyBullet ; spawn a new bullet, set fields
			bullet\x = enemy\x
			bullet\y = enemy\y
			bullet\dy = 10
			bullet\image = enemyBulletImage
			enemy\bulletTimer = MilliSecs() ; reset our timer
			If(enemyCount < 10) Then PlaySound enemyShoot
		EndIf 
	Next
End Function 

Function spawnSmallAsteroids(x,y)
	asteroid1.asteroid = New asteroid
	asteroid1\x = x
	asteroid1\y = y
	asteroid1\dx = Rand(-10,10)
	asteroid1\dy = Rand(-10,10)
	asteroid1\image = asteroidSmallImage
	asteroid1\isSmall = True 
End Function
	

Function updateAsteroids()
	If MilliSecs() - astTimer >= 5000 Then
		astTimer = MilliSecs()
		newAsteroid.asteroid = New Asteroid
		newAsteroid\x = Rand(0,1230)
		newAsteroid\y = 0
		newAsteroid\dx = Rand(-10,10)
		newAsteroid\dy = Rand(1,10)
		newAsteroid\image = asteroidImage
		newAsteroid\health = 100
		newAsteroid\isSmall = False
	EndIf 
	For asteroid.asteroid = Each asteroid
		DrawImage asteroid\image,asteroid\x,asteroid\y
		asteroid\x = asteroid\x + asteroid\dx
		asteroid\y = asteroid\y + asteroid\dy
		If(asteroid\x >= 1280 Or asteroid\x <= 0 Or asteroid\y >= 720 Or asteroid\y <= -50) Then
			Delete asteroid
		EndIf 		
	Next
End Function 