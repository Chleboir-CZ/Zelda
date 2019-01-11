package net.trdlo.zelda;

public interface CommandExecuter {

	/**
	 * Má se pokusit zpracovat příkaz, případné informace chrlit do dodané konzole, vrací, zda si věděl s příkazem rady
	 *
	 * @param command	příkaz na zpracování
	 * @param console	konzole, do které může dávat odezvu
	 * @return	zda příkaz zpracoval; pokud vrátí false, bude se příkazem zabývat někdo další
	 */
	boolean executeCommand(String command, Console console);
	
	/**
	 * Vypíše do konzole seznam příkazů a pokud je udán příkaz, pak jeho podrobnosti
	 * @param command 
	 * @param console 
	 */
	void listCommands(String command, Console console);
}
