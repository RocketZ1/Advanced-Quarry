package me.RocketZ1.AdvancedQuarry.Other;

import java.util.ArrayList;
import java.util.List;

public class PluginLang {

    public static String quarryName = "&6&lQuarry";
    public static List<String> quarryLore = new ArrayList<>();
    public static String invalidCmd = "&cInvalid Command!";
    public static String invalidCmdArgs = "&cInvalid Command! Try /advancedquarry";
    public static String invalidWorld = "&cYou cannot use a quarry in this world!";
    public static String noPerms = "&cYou do not have permission to execute this command!";
    public static String notOnline = "&cError: %player% is not online!";
    public static String itemGivenInvFull = "&a%player%'s inventory was full! Dropping quarry on ground!";
    public static String itemGiven = "&aQuarry was given to %player%!";
    public static String configReloaded = "&aConfig Reloaded!";
    public static String incorrectQuarryUserBreak = "&cThis quarry doesn't belong to you! It belongs to %player%!";
    public static String breakQuarryInProgress = "&cYou can't break this quarry while it's in progress!";
    public static String bypassOwnership = "&6Bypassed ownership, opening %player%'s Quarry.";
    public static String invalidOwner = "&cYou do not own this quarry! It belongs to %player%!";
    public static String missingItems = "&aMissing Items: ";
    public static String requiredItemsRemaining = "&b%item_name% remaining: &a%item_amount%";
    public static String requirementsMet = "&aMet all Requirements!";
    public static String valid = "&aValid!";
    public static String invalid = "&cNot valid!";
    public static String quarryProgressTxt = "&aQuarry Progress";
    public static String quarryProgress = "&6%progress%%";
    public static String quarryStopTimeMsg = "&bTime until quarry stops: ";
    public static String quarryRegionOverlap = "&cYou cannot place 2 quarries where their radius would overlap! Each quarry's radius is %radius%!";
    public static String notEnoughItems = "&cNot enough items in the chest!";
    public static String displayBorderON = "&6Display Border: &aON";
    public static String displayBorderOFF = "&6Display Border: &cOFF";
    public static String drillName = "&b-= &6&lDrill &b=-";

    public static String runQuarry = "&aRun Quarry";
    public static String stopQuarry = "&cStop Quarry";
    public static String displayBorder = "&6Display Border";
    public static String soundON = "&6Sound: &aON";
    public static String soundONLore = "&eSound is currently &aon&e. Click to toggle &coff&e.";
    public static String soundOFF = "&6Sound: &cOFF";
    public static String soundOFFLore = "&eSound is currently &coff&e. Click to toggle &aon&e.";

}
