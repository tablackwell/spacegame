Include "enemies.bb"
Include "asteroids.bb"
Include "powerups.bb"
Include "firstBoss.bb"

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
Global bossTimer = MilliSecs()

;Some gameplay logic code
Global score = 0 
Global difficulty = 1
Global elitesPresent = 0 
Global bossPresent = 0 

;powerup variables
Global powerupTimer = MilliSecs()
Global powerupActive$ = "none"
Global speedBoost = 0 

;load images and sound
Global loading = LoadImage("graphics\loading.bmp")

Global backgroundImageClose = LoadImage("graphics\stars.bmp")  
Global backgroundImageFar = LoadImage("graphics\starsfarther.bmp")


Global asteroidImage = LoadImage("graphics\asteroid.bmp")
Global asteroidSmallImage = LoadImage("graphics\asteroidsmall.bmp")

Global playerImage = LoadImage("graphics\player.bmp")
Global injuredPlayer = LoadImage("graphics\injuredPlayer.bmp")
Global nearDeath = LoadImage("graphics\playerNearDeath.bmp")
Global playerShielded = LoadImage("graphics\playerShielded.bmp")

Global bulletImage = LoadImage("graphics\bullet.bmp")
Global enemyBulletImage = LoadImage("graphics\enemybullet.bmp")

Global powerupHealth = LoadImage("graphics\powerupHealth.bmp")
Global powerupShoot = LoadImage("graphics\powerupShoot.bmp")
Global powerupSpeed = LoadImage("graphics\powerupSpeed.bmp")
Global powerupShield = LoadImage("graphics\powerupShield.bmp")

Global enemyImage = LoadImage("graphics\enemy.bmp")
Global enemyElite = LoadImage("graphics\enemyElite.bmp")

Global damageSound = LoadSound("sfx\damage.wav")
SoundVolume damagesound,.5
Global playerShoot = LoadSound("sfx\playershoot.wav")
SoundVolume playerShoot,.5
Global enemyShoot = LoadSound("sfx\enemyshoot.wav")
SoundVolume enemyShoot,.5
Global asteroidExplosion = LoadSound("sfx\asteroidExplosion.wav")
Global powerupHealthSound = LoadSound("sfx\powerupHealth.wav")
Global powerupShieldSound = LoadSound("sfx\powerupShield.wav")
Global powerupShootSound = LoadSound("sfx\powerupShoot.wav")
Global powerupSpeedSound = LoadSound("sfx\powerupSpeed.wav")

Global soundOff = False 
Global scrolly = 0 ;For parallax 

;establish our constants for controls. Now WASD instead because its nicer
Const ESCKEY = 1, UPKEY = 17, LEFTKEY = 30, RIGHTKEY = 32, DOWNKEY = 31, SPACEBAR = 28


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

.main
;main game loop
While KeyDown(1)=0 	
	;Music 
	If Not ChannelPlaying(music)
		music = PlayMusic("music\afterburner.mp3")
	EndIf 
	
	;Graphics and text
	updateBackground()
	dispInfo() 
	
	;Game logic function calls 
	If bossPresent = 0 
		updateEnemy() ; Enemy movement and shooting
		updatePlayer() ; Player movement (keyboard) and shooting
		updateBullets() ; Check bullet collisions and movement
		updateEnemyBullets() ; Check enemy bullet collisions and movement
		updateAsteroids()
		updatePowerups()
		checkScore() 
		
		
	ElseIf bossPresent = 1 ; First boss
		updateBoss()
		StopChannel music 
		If Not ChannelPlaying(bossMusic)
			bossMusic = PlayMusic("music\terminal.mp3")
		EndIf 
		
		If (MilliSecs() - bossTimer ) >= 25000 Then
			difficulty = 5
		Else 
			Text 600,360,"BOSS BATTLE START: " + Str( 25 - (MilliSecs() - bossTimer) / 1000)
			difficulty = 1 
		EndIf 
		
		updatePlayer()
		updateBullets()
		
	EndIf 
	Flip
Wend





; Shows info on the screen related to the game (some of it is debug)
Function dispInfo() 
	Text 200,700,"Time Elapsed: " + Str((MilliSecs() - timer) / 1000)
	Text 400,700,"Enemy Count: " + Str(enemyCount)
	Text 600,700,"Player Health: " + Str(player\health)
	Text 800,700,"Score: " + Str(score)
	If bossPresent = 0 Then 
		Text 900,700,"Difficulty: " + Str(difficulty)
	Else Text 900,700,"BOSS BATTLE"
	EndIf 
	Text 1100,700,powerupActive$ ; Debug, remove for final version
End Function

; Enables a scrolling background (yay parallax)
Function updateBackground() ; 
	TileImage backgroundimageFar,0,scrolly ;Background
	TileImage backgroundImageClose,0,scrolly*2 ;Foreground, moves faster
	scrolly = scrolly+difficulty ; Scroll forward - Increases speed by difficulty!
	If(scrolly >= ImageHeight(backgroundimageclose)) Then
		scrolly = 0 ;Reset if needed
	EndIf
End Function

Function updatePlayer()
	DrawImage player\image,player\x,player\y
	
	If powerUpActive = "boostSpeed" Then 
		speedBoost = 4
	EndIf 
	If MilliSecs() - powerUpTimer >= 10000 Then
		powerUpActive = "none"
		speedBoost = 0 
	EndIf 
	
	If (MilliSecs() - powerUpTimer >= 5000) And powerUpActive = "boostShoot"
		powerupActive = "none"
	EndIf 
	
	If powerupActive = "boostShield" Then
		player\image = playershielded
	Else If player\health > 250 Then 
		player\image =  playerImage
	Else If player\health <= 250 And player\health > 125 Then
		 player\image = injuredplayer
	Else If player\health <= 125 Then 
		player\image = nearDeath
	EndIf 
	
	If player\health >= 0 Then ; Only if the player is still alive
		;Keyboard controls
		If KeyDown(LEFTKEY)
			player\x = player\x - (7 + difficulty + speedBoost)		
		EndIf
		If KeyDown(RIGHTKEY)
			player\x = player\x + (7 + difficulty + speedBoost)
		EndIf
		
		If KeyDown(UPKEY)
			player\y = player\y - (5 + difficulty + speedBoost)
		EndIf
		If KeyDown(DOWNKEY)
			player\y = player\y + (5 + difficulty + speedBoost)
		EndIf
		
		If Not (powerupactive = "boostShoot")
			If KeyHit(SPACEBAR) ;If we shoot, make a new bullet
				bullet.bullet = New bullet
				bullet\image = bulletImage
				bullet\dy = -10
				bullet\x = player\x
				bullet\y = player\y					
				PlaySound playerShoot
			EndIf 
		Else
			If KeyDown(SPACEBAR) ;If we shoot, make a new bullet
				bullet.bullet = New bullet
				bullet\image = bulletImage
				bullet\dy = -10
				bullet\x = player\x
				bullet\y = player\y
				
				;PlaySound playerShoot commented out because ears
			EndIf 
		EndIf 
	
	Else ; The player is dead, so its game over
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
						If enemy\isElite = True Then
					 		eliteCount = eliteCount - 1 	
							spawnPowerup(enemy\x,enemy\y,0,5)
						EndIf 
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


Function checkScore()
	If (MilliSecs() - scoreTimer) >= 1000 Then
		score = score + 1 
		scoreTimer = MilliSecs()
	EndIf 
	If (MilliSecs() - enemyTimer) >= (5000 / difficulty) Then
		enemyTimer = MilliSecs()
		spawnEnemy(1)
	EndIf 
	If score > 100 And score < 200 Then
		difficulty = 2
	Else If score > 200 And score < 500 Then
		difficulty = 3
	Else If score >= 500 And score < 1000 Then
		difficulty = 4
	Else If score > 1000 And score < 2000 Then
		difficulty = 5 
	Else If score >= 2000 And score < 3000 Then
		difficulty = 6
	Else If score >= 3000 And score < 5000 Then
		difficulty = 7
	Else If score >= 5000 Then
		bossTimer = MilliSecs()
		bossPresent = 1
		wipeEntities()
		difficulty = 1
		player\x = 600
		player\y = 670
		player\health = 500
	EndIf 
End Function

Function triggerBoss()
	bossPresent = 1
	difficulty = 1
End Function

Function wipeEntities()
	For bullet.bullet = Each bullet
		Delete bullet
	Next
	For enemy.enemy = Each enemy
		Delete enemy
	Next
	For asteroid.asteroid = Each asteroid
		Delete asteroid
	Next
	For enemybullet.enemybullet = Each enemybullet
		Delete enemybullet
	Next
	For powerup.powerup = Each powerup
		Delete powerup
	Next
End Function

StopChannel music 
End 