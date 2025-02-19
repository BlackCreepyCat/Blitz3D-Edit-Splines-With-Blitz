Const MAX_POINTS = 20

Global segments = 20

Type Point
	Field x #
	Field y #
End Type

Function Point_Create .Point(x #, y #)
	Local lPoint .Point = New Point
	lPoint\ x = x
	lPoint\ y = y
	Return lPoint
End Function

Function Point_Destroy(point .Point)
	If point <> Null Then
		Delete point
	EndIf
End Function

Type Curve
	Field a .Point
	Field b .Point
End Type

Function Curve_Create.Curve(ax #, ay #, bx #, by #)
	Local lCurve .Curve = New Curve
	lCurve\ a = Point_Create(ax, ay)
	lCurve\ b = Point_Create(bx, by)
	Return lCurve
End Function

Function Curve_Destroy(curve .Curve)
	If curve <> Null Then
		Point_Destroy(curve\ a)
		Point_Destroy(curve\ b)
		Delete curve
	EndIf
End Function

Type Figure
	Field pointCount%
	Field points .Point[MAX_POINTS - 1]
	Field curves .Curve[MAX_POINTS - 1]
End Type

Function Figure_Create.Figure()
	Local lFigure .Figure = New Figure
	lFigure\ pointCount = 0
	Return lFigure
End Function

Function Figure_Destroy(figure .Figure)
	If figure <> Null Then
		For i = 0 To figure\ pointCount
			Point_Destroy(figure\ points[i])
			Curve_Destroy(figure\ curves[i])
		Next
	EndIf
End Function

Function Figure_AddPoint(figure .Figure, x #, y #, ax #, ay #, bx #, by #)
	If figure <> Null Then
		If figure\ pointCount < MAX_POINTS - 1 Then
			figure\ points[figure\ pointCount] = Point_Create(x, y)
			figure\ curves[figure\ pointCount] = Curve_Create(ax, ay, bx, by)
			figure\ pointCount = figure\ pointCount + 1
		EndIf
	EndIf
End Function

Function Figure_Show(figure .Figure)
	Local a %, b %, x1 #, y1 #, x2 #, y2 #, i #, j #, s %
	For a = 0 To figure\ pointCount - 1
		b = (a + 1) Mod figure\ pointCount
		s = 0
		If showAnchors Then
			Color 50, 255, 50
			Oval figure\ points[a]\ x - 3, figure\ points[a]\ y - 3, 6, 6, 0
			Line figure\ points[a]\ x, figure\ points[a]\ y, figure\ points[a]\ x + figure\ curves[a]\ a\ x, figure\ points[a]\ y + figure\ curves[a]\ a\ y
			Line figure\ points[b]\ x, figure\ points[b]\ y, figure\ points[b]\ x + figure\ curves[a]\ b\ x, figure\ points[b]\ y + figure\ curves[a]\ b\ y
			Oval figure\ points[a]\ x + figure\ curves[a]\ a\ x - 2, figure\ points[a]\ y + figure\ curves[a]\ a\ y - 2, 4, 4, 0
			Oval figure\ points[b]\ x + figure\ curves[a]\ b\ x - 2, figure\ points[b]\ y + figure\ curves[a]\ b\ y - 2, 4, 4, 0
		EndIf
		Color 255, 255, 255
		x1 = figure\ points[a]\ x
		y1 = figure\ points[a]\ y
		While s <= segments
			i = Float#(s) / segments
			j = (1 - i)
			x2 = x1
			y2 = y1
			x1 = j * j * j * figure\ points[a]\ x + 3 * i * j * j * (figure\ points[a]\ x + figure\ curves[a]\ a\ x) + 3 * i * i * j * (figure\ points[b]\ x + figure\ curves[a]\ b\ x) + i * i * i * figure\ points[b]\ x
			y1 = j * j * j * figure\ points[a]\ y + 3 * i * j * j * (figure\ points[a]\ y + figure\ curves[a]\ a\ y) + 3 * i * i * j * (figure\ points[b]\ y + figure\ curves[a]\ b\ y) + i * i * i * figure\ points[b]\ y
			Line x1, y1, x2, y2
			s = s + 1
		Wend
	Next
End Function

Graphics 1024,768,0,2
SetBuffer BackBuffer()
SeedRnd MilliSecs()

Global showAnchors = True
Global mouseOverID = 0, mouseClickID = 0, mouseOverOffsetX = 0, mouseOverOffsetY = 0, mouseClickOffsetX, mouseClickOffsetY

; Kreis erstellen
circle .Figure = Figure_Create()
Figure_AddPoint(circle, 200, 200,   0, -50, -50,   0)
Figure_AddPoint(circle, 300, 100,  50,   0,   0, -50)
Figure_AddPoint(circle, 400, 200,   0,  50,  50,   0)
Figure_AddPoint(circle, 300, 300, -50,   0,   0,  50)

; Quadrat erstellen
rectangle .Figure = Figure_Create()
Figure_AddPoint(rectangle, 350, 350,  10,   0, -10,   0)
Figure_AddPoint(rectangle, 450, 350,   0,  10,   0, -10)
Figure_AddPoint(rectangle, 450, 450, -10,   0,  10,   0)
Figure_AddPoint(rectangle, 350, 450,   0, -10,   0,  10)

; Tropfen erstellen
drop .Figure = Figure_Create()
Figure_AddPoint(drop, 500, 500, 45, 60, -45, 60)

; Stern erstellen
starEdges = 6
star .Figure = Figure_Create()
For i = 1 To starEdges
	Figure_AddPoint(star, 650 + Cos(i * 360 / starEdges) * 120, 300 + Sin(i * 360 / starEdges) * 120, Cos(i * 360 / starEdges + 170) * 90, Sin(i * 360 / starEdges + 170) * 90, Cos(i * 360 / starEdges - 115) * 90, Sin(i * 360 / starEdges - 115) * 90)
Next

While Not KeyDown(1)
	Cls
	segments = segments + MouseZSpeed()
	If segments < 1 Then segments = 1
	If segments > 50 Then segments = 50
	If MouseHit(2) Then showAnchors = 1 - showAnchors
	Text 1, 1, "Segmente: " + segments + " (+/- Mausrad)"
	Text 1, 20, "Bewege Anker (Maus links)"
	Text 1, 40, "Zeige/Verberge Anker (Maus rechts)"
	foo
	Flip
Wend
End

Function foo()
	Local lFigure .Figure, i %
	For lFigure = Each Figure
		Figure_Show(lFigure)
		For i = 0 To lFigure\ pointCount - 1
			If (MouseX() - lFigure\ points[i]\ x) ^ 2 + (MouseY() - lFigure\ points[i]\ y) ^ 2 < 25 Then mouseOverID = Handle(lFigure\ points[i]) : mouseOverOffsetX = 0 : mouseOverOffsetY = 0
			If Not mouseOverID Then If (MouseX() - lFigure\ points[i]\ x - lFigure\ curves[i]\ a\ x) ^ 2 + (MouseY() - lFigure\ points[i]\ y - lFigure\ curves[i]\ a\ y) ^ 2 < 25 Then mouseOverID = Handle(lFigure\ curves[i]\ a) : mouseOverOffsetX = lFigure\ points[i]\ x : mouseOverOffsetY = lFigure\ points[i]\ y
			If Not mouseOverID Then j = (i + 1) Mod lFigure\ pointCount : If (MouseX() - lFigure\ points[j]\ x - lFigure\curves[i]\ b\ x) ^ 2 + (MouseY() - lFigure\ points[j]\ y - lFigure\ curves[i]\ b\ y) ^ 2 < 25 Then mouseOverID = Handle(lFigure\ curves[i]\ b) : mouseOverOffsetX = lFigure\ points[j]\ x : mouseOverOffsetY = lFigure\ points[j]\ y
		Next
	Next
	If MouseHit(1) Then mouseClickID = mouseOverID : mouseClickOffsetX = mouseOverOffsetX : mouseClickOffsetY = mouseOverOffsetY
	If MouseDown(1) Then
		If Not mouseClickID = 0 Then
			lPoint .Point = Object.Point(mouseClickID)
			lPoint\ x = MouseX() - mouseClickOffsetX
			lPoint\ y = MouseY() - mouseClickOffsetY
		EndIf
	Else
		mouseClickID = 0
		mouseOverID = 0
	EndIf
End Function
;~IDEal Editor Parameters:
;~F#4
;~C#Blitz3D