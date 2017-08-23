
AppTitle "DETERMINATION"
Graphics 1280,720,32,1
AutoMidHandle True ; Basically, images will be placed based on center, not left corner
SetBuffer BackBuffer() ; a back buffer

SeedRnd = MilliSecs() ; set up our random number generator
Global timer = MilliSecs() ; make an overall timer
Global enemyCount = 0 
Global astTimer = MilliSecs()
Global scoreTimer = MilliSecs()
Global enemyTimer = MilliSecs()
Global score = 0 
Global difficulty = 1
Global elitesPresent = 0 

;load images and sound
Global loading = LoadImage("graphics\loading.bmp")

Global backgroundImageClose = LoadImage("graphics\stars.bmp")
Global backgroundImageFar = LoadImage("graphics\starsfarther.bmp")

Global asteroidImage = LoadImage("graphics\asteroid.bmp")
Global asteroidSmallImage = LoadImage("graphics\asteroidsmall.bmp")

Global playerImage = LoadImage("graphics\player.bmp")
Global injuredPlayer = LoadImage("graphics\injuredPlayer.bmp")
Global nearDeath = LoadImage("graphics\playerNearDeath.bmp")

Global bulletImage = LoadImage("graphics\bullet.bmp")
Global enemyBulletImage = LoadImage("graphics\enemybullet.bmp")
Global powerupimage = LoadImage("graphics\powerup.bmp")

Global enemyImage = LoadImage("graphics\enemy.bmp")
Global enemyElite = LoadImage("graphics\enemyElite.bmp")

Global damageSound = LoadSound("sfx\damage.wav")
Global playerShoot = LoadSound("sfx\playershoot.wav")
Global enemyShoot = LoadSound("sfx\enemyshoot.wav")
Global asteroidExplosion = LoadSound("sfx\asteroidExplosion.wav")

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

Type powerup
	Field x,y,dx,dy
	Field image
	Field mode
End Type 

Type enemyBullet
	Field x,y,dy,dx
	Field image
End Type 

Type enemy
	Field x,y,dx,dy,health
	Field image
	Field isDead
	Field bulletTimer 
	Field isElite
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
		music = PlayMusic("music\afterburner.mp3")
	EndIf 
	updateBackground() ; Parallax stuff
	Text 200,700,"Time Elapsed: " + Str((MilliSecs() - timer) / 1000)
	Text 400,700,"Enemy Count: " + Str(enemyCount)
	Text 600,700,"Player Health: " + Str(player\health)
	Text 800,700,"Score: " + Str(score)
	Text 900,700,"Difficulty: " + Str(difficulty)
	If (MilliSecs() - scoreTimer) >= 1000 Then
		score = score + 1 
		scoreTimer = MilliSecs()
	EndIf 
	If (MilliSecs() - enemyTimer) >= (5000 / difficulty) Then
		enemyTimer = MilliSecs()
		spawnEnemy(1)
	EndIf 
	updateEnemy() ; Enemy movement and shooting
	updatePlayer() ; Player movement (keyboard) and shooting
	updateBullets() ; Check bullet collisions and movement
	updateEnemyBullets() ; Check enemy bullet collisions and movement
	updateAsteroids()
	updatePowerups()
	checkScore() 
	Flip
Wend
StopChannel music 

Function updateBackground() ; Controls the background
	TileImage backgroundimageFar,0,scrolly ;Background
	TileImage backgroundImageClose,0,scrolly*2 ;Foreground, moves faster
	scrolly = scrolly+difficulty ; Scroll forward 
	If(scrolly >= ImageHeight(backgroundimageclose)) Then
		scrolly = 0 ;Reset if needed
	EndIf
End Function

Function updatePlayer()
	DrawImage player\image,player\x,player\y
	If player\health > 250 Then player\image =  playerImage
	If player\health <= 250 Then player\image = injuredplayer
	If player\health <= 125 Then player\image = nearDeath
	If player\health >= 0 Then 
		;Keyboard controls
		If KeyDown(LEFTKEY)
			player\x = player\x - (7 + difficulty)			
		EndIf
		If KeyDown(RIGHTKEY)
			player\x = player\x + (7 + difficulty)
		EndIf
		
		If KeyDown(UPKEY)
			player\y = player\y - (5 + difficulty)
		EndIf
		If KeyDown(DOWNKEY)
			player\y = player\y + (5 + difficulty)
		EndIf
		
		If KeyHit(SPACEBAR) ;If we shoot, make a new bullet
			bullet.bullet = New bullet
			bullet\image = bulletImage
			bullet\dy = -10
			bullet\x = player\x
			bullet\y = player\y
			PlaySound playerShoot
		EndIf 
	Else
		Text 640,360,"GAME OVER"
		Text 640,460,"Press ESC to quit"
			
	End If 
	; Some other stuff not related to player movement
	
	If KeyHit(18) ; Enemy respawn feature for testing
		spawnEnemy(1)
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
					enemy\health = enemy\health - 25
					If enemy\health <= 0 Then 
						If enemy\isElite = True Then eliteCount = eliteCount - 1 	
						Delete enemy
						enemyCount = enemyCount - 1
					EndIf 	
					score = score + 25
					PlaySound damageSound 
					bullet\invis = True 
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
		bullet\x = bullet\x + bullet\dx
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
			If enemy\isElite Then spawnHomingBullet(enemy\x,enemy\y,0,5)
			enemy\bulletTimer = MilliSecs() ; reset our timer
			If(enemyCount < 10) Then PlaySound enemyShoot
		EndIf 
	Next
End Function 

Function spawnHomingBullet(x$,y$,dx$,dy$)
	bullet2.enemyBullet = New enemyBullet
	bullet2\x = x
	bullet2\y = y
	bullet2\dy = dy
	bullet2\dx = dx
	bullet2\image = enemyBulletImage
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
	;roll the dice to see if we spawn a powerup
	If Rand(1,10) = 10 Then
		spawnPowerUp(x,y,Rand(0,5),Rand(0,5))
	EndIf 
End Function
	
Function spawnEnemy(count)
	For i = 1 To count
		enemy.Enemy = New Enemy 
		enemy\x = Rand(0,1230)
		coinFlip = Rand(0,1)
		If coinFlip Then
			enemy\y = 50
		Else
			enemy\y = 100 	
		EndIf 	
		enemy\dy = 0 			
		coinFlip2 = Rand(0,1)
		If coinflip2 Then 
			enemy\dx = 8
		Else enemy\dx = -8 
		EndIf 
		enemy\isDead = False
		enemy\image = enemyImage 
		enemy\bulletTimer = MilliSecs()
		enemy\health = 25
		enemyCount = enemyCount + 1 
	Next
	If(difficulty >= 2 ) And (Rand(0,10) = 10) And elitesPresent < difficulty Then
		enemy.Enemy = New Enemy 
		enemy\x = Rand(0,1230)
		coinFlip = Rand(0,1)
		If coinFlip Then
			enemy\y = 50
		Else
			enemy\y = 100 	
		EndIf 	
		enemy\dy = 0 			
		coinFlip2 = Rand(0,1)
		If coinflip2 Then 
			enemy\dx = 8
		Else enemy\dx = -8 
		EndIf 
		enemy\isDead = False
		enemy\image = enemyElite
		enemy\bulletTimer = MilliSecs()
		enemyCount = enemyCount + 1 
		enemy\health = 100
		enemy\isElite = True
		elitesPresent = elitesPresent + 1 
	EndIf 

End Function

Function updateAsteroids()
	; Normal asteroid spawning
	If MilliSecs() - astTimer >= 2000 Then
		astTimer = MilliSecs()
		spawnAsteroids(difficulty)
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

Function spawnAsteroids(count)
	For i = 1 To count
		newAsteroid.asteroid = New Asteroid
		newAsteroid\x = Rand(0,1230)
		newAsteroid\y = 0
		newAsteroid\dx = Rand(-5,5)
		newAsteroid\dy = Rand(1,7)
		newAsteroid\image = asteroidImage
		newAsteroid\health = 100
		newAsteroid\isSmall = False
	Next 
End Function

Function spawnPowerup(x,y,dx,dy)
	powerup.powerup = New powerup
	powerup\image = powerupImage
	powerup\x = x
	powerup\y = y
	powerup\dx = dx
	powerup\dy = dy
End Function

Function updatePowerups()
	For powerup.powerup = Each powerup
		DrawImage powerup\image,powerup\x,powerup\y
		powerup\x = powerup\x + powerup\dx
		powerup\y = powerup\y + powerup\dy
		If powerup\x <= 0 Or powerup\x >= 1280 Or powerup\y <= 0 Or powerup\y >= 720 Then
			Delete powerup
		Else
			If ImagesOverlap(player\image,player\x,player\y,powerup\image,powerup\x,powerup\y) Then
				player\health = player\health + 50
				Delete powerup
			EndIf 
		EndIf 
	Next  
	
End Function

Function checkScore()
	If score > 100 And score < 200
		difficulty = 2
	Else If score > 200 And score < 500
		difficulty = 3
	Else If score > 500 And score < 1000
		difficulty = 4
	Else If score > 1000
		difficulty = 5 
	EndIf 
End Function