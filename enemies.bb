
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
				If Not powerupactive = "boostShield"
					player\health = player\health - 10 
				EndIf 
				Delete bullet 
			EndIf
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
