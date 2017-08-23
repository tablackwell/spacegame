Graphics 1280,720,32,1
AutoMidHandle True ; Basically, images will be placed based on center, not left corner
SetBuffer BackBuffer() ; a back buffer
SeedRnd = MilliSecs() ; set up our random number generator
Global timer = MilliSecs() ; make an overall timer
Global enemyCount = 0 
Global astTimer = MilliSecs()

Global score = 0 

;load images and sound
Global loading = LoadImage("loading.bmp")
Global backgroundImageClose = LoadImage("stars.bmp")
Global backgroundImageFar = LoadImage("starsfarther.bmp")
Global asteroidImage = LoadImage("asteroid.bmp")
Global asteroidSmallImage = LoadImage("asteroidsmall.bmp")
Global playerImage = LoadImage("player.bmp")
Global injuredPlayer = LoadImage("injuredPlayer.bmp")
Global nearDeath = LoadImage("playerNearDeath.bmp")
Global bulletImage = LoadImage("bullet.bmp")
Global enemyBulletImage = LoadImage("enemybullet.bmp")
Global enemyImage = LoadImage("enemy.bmp")
Global damageSound = LoadSound("damage.wav")
Global playerShoot = LoadSound("playershoot.wav")
Global enemyShoot = LoadSound("enemyshoot.wav")
Global asteroidExplosion = LoadSound("asteroidExplosion.wav")

Global soundOff = False 

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
	Field invis
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
player\health = 500

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
	Text 600,700,"Player Health: " + Str(player\health)
	Text 800,700,"Score: " + Str(score)
	updateEnemy() ; Enemy movement and shooting
	updatePlayer() ; Player movement (keyboard) and shooting
	updateBullets() ; Check bullet collisions and movement
	updateEnemyBullets() ; Check enemy bullet collisions and movement
	updateAsteroids()
	checkScore()
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
	If player\health <= 250 Then player\image = injuredplayer
	If player\health <= 125 Then player\image = nearDeath
	If player\health <= 0 Then 
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
	EndIf 
	
	; Some other stuff not related to player movement
	
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
	If KeyHit(25) ; turn sound off/on
		If soundOff
			ResumeChannel music 
		Else
			PauseChannel music
		EndIf 
		soundOff = Not soundOff
	EndIf 
		
End Function 

Function updateBullets() 
	For bullet.bullet = Each bullet ;For ALL bullets in memory
		DrawImage(bullet\image,bullet\x,bullet\y)
		bullet\y = bullet\y + bullet\dy ; move it!
		If(bullet\y <= 0) Then ; Delete if out of bounds 
			Delete bullet
		Else If Not bullet\invis
			For enemy.enemy = Each enemy ; Check for collisions with enemies
				If ImagesOverlap(bullet\image,bullet\x,bullet\y,enemy\image,enemy\x,enemy\y)
					Delete enemy
					enemyCount = enemyCount - 1 
					score = score + 25
					PlaySound damageSound 
				EndIf 
			Next
			For asteroid.asteroid = Each asteroid
				If ImagesOverlap(bullet\image,bullet\x,bullet\y,asteroid\image,asteroid\x,asteroid\y)
					If(Not asteroid\isSmall) Then
						spawnSmallAsteroids(asteroid\x,asteroid\y)
						score = score + 5
					Else
						score = score + 1 
					EndIf 
					Delete asteroid
					PlaySound asteroidExplosion
					bullet\invis = True  
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
				player\health = player\health - 10 
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
		If(MilliSecs() - enemy\bulletTimer >= 2000) Then ; Once every 2 seconds
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
	amount = Rand(2,6)
	For i = 0 To amount
		asteroid1.asteroid = New asteroid
		asteroid1\x = x
		asteroid1\y = y
		asteroid1\dx = Rand(-10,10)
		asteroid1\dy = Rand(-10,10)
		asteroid1\image = asteroidSmallImage
		asteroid1\isSmall = True 
	Next
End Function
	

Function updateAsteroids()
	If MilliSecs() - astTimer >= 2000 Then
		astTimer = MilliSecs()
		newAsteroid.asteroid = New Asteroid
		newAsteroid\x = Rand(0,1230)
		newAsteroid\y = 0
		newAsteroid\dx = Rand(-5,5)
		newAsteroid\dy = Rand(1,7)
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
		ElseIf ImagesOverlap(asteroid\image,asteroid\x,asteroid\y,player\image,player\x,player\y)
			If Not asteroid\isSmall Then 
				spawnSmallAsteroids(asteroid\x,asteroid\y)
				player\health = player\health - 20
			Else 
				player\health = player\health - 5 
			EndIf 
			Delete asteroid 
			PlaySound asteroidExplosion
		EndIf 
	Next
End Function 

Function checkScore()
End Function