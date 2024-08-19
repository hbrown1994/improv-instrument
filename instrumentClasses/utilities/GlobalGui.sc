GlobalGui {
	classvar <>slotWindows, <>window, <>rangeSliders, <>invertButtons;

	/*____________Constructors____________*/
	*new {^super.new.init}

	init {
		slotWindows=List.new;
		rangeSliders=Array.newClear(16);
		invertButtons=List.new;

		window = Window.new("Controls", Rect(0, 260, 480, 430)).front;  //Make window
		window.view.decorator = FlowLayout( window.view.bounds, 10@10, 20@5 );

		16.do{|i|
			slotWindows.add(
				Window.new("scale"++i, Rect(0, 260, 730, 51*8));
			);

			slotWindows[i].view.decorator=FlowLayout(slotWindows[i].view.bounds);
			slotWindows[i].view.decorator.gap=10@10;
		};

		16.do{|i|
			Button(window.view, Rect(5, 5, 100, 100))
			.states_([["slot"++i, Color.black, [Color.red, Color.green, Color.blue, Color.yellow][i%4]], ["slot"++i, Color.black, Color.white]])
			.action_({arg view;
				if(view.value==1, {slotWindows[i].visible=true}, {slotWindows[i].visible=false});
			});
		};
	}
}
