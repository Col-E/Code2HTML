package me.coley.c2h;

import javafx.application.Application;
import me.coley.c2h.ui.RootUI;

/**
 * Main, pointing to UI {@link RootUI}.
 *
 * @author Matt Coley
 */
public class Code2Html {
	public static void main(String[] args) {
		// TODO: Re-implement CLI
		//  - Options
		//    - language (default first lang in config)
		//    - outClipboard
		//    - outFile
		//    - inline css
		//  - Arg
		//    - 0: file
		//  - No args indicating CLI intended, then open GUI

		Application.launch(RootUI.class, args);
	}
}
