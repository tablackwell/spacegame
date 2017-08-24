
Type powerup
	Field x,y,dx,dy
	Field image
	Field mode
End Type 

Function spawnPowerup(x,y,dx,dy)
	SeedRnd = MilliSecs()
	powerup.powerup = New powerup
	powerup\x = x
	powerup\y = y
	powerup\dx = dx
	powerup\dy = dy
	powerup\mode = Rand(1,4)
	If powerup\mode = 1 Then
		powerup\image = powerupHealth
	ElseIf powerup\mode = 2 Then
		powerup\image = powerupShoot
	ElseIf powerup\mode = 3 Then
		powerup\image = powerupSpeed
	ElseIf powerup\mode = 4 Then
		powerup\image = powerupShield
	EndIf 
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
				applyPowerup(powerup\mode)
				Delete powerup
			EndIf 
		EndIf 
	Next  
	
End Function

Function applyPowerup(mode)
	
	If mode = 1 Then
		player\health = player\health + 50 
	; The following are placeholders
	ElseIf mode = 2 Then
		player\health = player\health + 50
		boostShoot()
	ElseIf mode = 3 Then
		player\health = player\health + 50 
		boostSpeed()
	ElseIf mode = 4 Then
		player\health = player\health + 50 
		boostShield()
	EndIf 
End Function

Function boostShoot()
	powerupactive = "boostShoot" ; change the mode 
	powerupTimer = MilliSecs() ;start the timer
End Function

Function boostSpeed()
	powerUpActive = "boostSpeed"
	powerupTimer = MilliSecs()
End Function

Function boostShield()
	powerupActive = "boostShield"
	powerupTimer = MilliSecs()
End Function
