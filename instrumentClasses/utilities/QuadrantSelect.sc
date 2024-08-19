QuadrantSelect {
	*kr{
		arg x, y, xMax, yMax;
		^Select.kr(x>(xMax/2), [Select.kr(y<(yMax/2), [0, 3]), Select.kr(y<(yMax/2), [1, 2])]);
	}
}