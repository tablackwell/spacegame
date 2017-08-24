



Type asteroid
	Field x,y,dx,dy,health
	Field image
	Field isSmall
End Type 

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
				If Not powerupactive = "boostShield" Then
					player\health = player\health - 20
				EndIf 
			Else 
				If Not powerupactive = "boostShield" Then
					player\health = player\health - 5 
				EndIf 
			EndIf 
			Delete asteroid 
			PlaySound asteroidExplosion
		EndIf 
	Next
End Function 

