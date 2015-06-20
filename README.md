#RepeatedPhrases  
##A tool for boiled leather and insights

Some phrases are repeated in A Song of Ice and Fire.  Some [are suggestive as hell.](http://redd.it/30y8ez)  Most [are not.](http://imgur.com/75joFxC)  A few [are in the middle.](http://imgur.com/z789AIe)  Really the only way to know which is the case is [to view them in context](http://imgur.com/YbHU0zS), and that's exactly what I'm giving you to the power to do.

[LINK]()

I made this tool that lets you start with ebooks of ASOIAF and end up with these individual chapter files that provide clickable links from one instance of a repeated phrase to the next so you can easily determine something's significance based on its use in its original context and its use in another context. [Here's a demonstration](http://imgur.com/bqX7mpJ) pertaining to the repeated phrase mentioned in the first link in the first paragraph.  Occasionally, these connections just [provide irony.](http://imgur.com/hoZDV0c)

---
##Requirements

| Requirement | Purpose | Note |
|:--|:--|:--|
| ASOIAF ebooks |  | Ignore WOIAF. |
| [Calibre](http://calibre-ebook.com/) | convert mobi et al. to html | Other conversion software is fine. It's free. |
| [7-Zip](http://www.7-zip.org/download.html) | unzip HTMLZ files | It's free. |
| [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html) | to run the code | It's free. Java compiles to bytecode instead of pure machine code. I used lambda expressions, which Java 7 doesn't support |
| 400MB (~ish) RAM available | an increased amount of memory is needed during repeated-phrase analysis |  |

---
##Instructions

* Add the ebooks to your Calibre library.  
* Convert them to HTMLZ.  
* Go to the folders where those HTMLZ files are located (In Calibre, right click the book and choose "Open containing folder".) and unzip them (to their own new folders)

* Choose/Create a work folder, which I call **work_folder** in these instructions.
* Download [the project]() as a ZIP, unzip it, and copy/move/save **repeatedphrases.jar** to/into/in **work_folder**
* Double-click the **repeatedphrases.jar** file in **work_folder** to run the application.

After a few seconds, a small, poorly-designed window will pop up, named "Repeated Phrase Analyser".  The second line from the top of the window names the folder from/in which the application is operating.  If this isn't your chosen **work_folder**, then something's wrong (with my code).

* Click "Create Folders".

This makes the folders that this application needs in order to work. Of particular importance is **00_html_books**.  Once these folders have been created, copy each ebook's unzipped **index.html** file (still in the Calibre library) into **00_html_books** and rename them so they indicate what book they belong to. The exact names they need to have for the application to work follow:

1. AGOT.html
1. ACOK.html
1. ASOS.html
1. AFFC.html
1. ADWD.html
1. DE_0.html
1. DE_1.html
1. DE_2.html
1. PQ.html
1. RP.html

**Note**: If you don't have all of these books, it doesn't matter, and the application will run just fine.

* Specify a trail-file (I recommend moving at least one from the unzipped project into **work_folder**), and click "Chapterize books; Add links".

This will take most of an hour to run.

If you don't specify a minimum linked phrase size, it'll default to 3, which is a decent balance, I think.  You can even set it to something over 218 to eliminate all repeated-phrase links and leave yourself with nothing but pure chapters (not recommended).

Once it finishes, the GUI's Status will start with "Done:".

* copy **style.css** into **12_readable** and open the html chapters in a browser.

If the style (such as the text colors) is not to your liking, you can open **style.css** in a text editor and change it.  For example, changing the **color: #??????;** values in the CSS file (in the section titled /\*GENERAL CONCERNS\*/) to some other colors, such as **color: blue;** or **color: white;**.  Don't forget the **semicolon** (;).

---
##Switching to/from Boiled Leather, Ball of Beasts, Etc.

If and when you choose to read with a different chapter order or with a different limit for the linked phase size, run the application, specify a trail-file that has the chapter-order you want, and click "Change Chapter Order" or "Change Trail (Keep Link Order)" if you don't care about the chapter sequence assumed by the in-text links.

---
##Trail-file format

A trail file is made of three tab-delimited columns: Previous Chapter; Current Chapter; Next Chapter  
The entries are specified as .html filenames for the chapters affected.

I chose this redundant method to make it possible to control whether you want the first/last chapter to link to the last/first, or whatever, rather than forcing everything to link or to not link.  I've included a trial file (**individual_books.txt**) that takes advantage of this (if you can call it that) to make each book's final and first chapters not link to a next or previous (respectively) chapter.

With Excel, it's pretty easy to just copy and past the central column on the left and right side, shifted down or up one cell as needed and then save as tab-delimited text; so, feel free to make your own.

---
##Notes on the application's operation

After the folders 00_ through 12_ are created, this application's operation is a series of island-hops from one folder to the next, transforming the content of the books in distinct steps.

1. 00-01: Newlines are added to ensure mild human-readability if you choose to look into the source HTML (like for messing with CSS classes). This helps step 4 go faster by shortening line lengths, since it goes line-by-line.

2. 01-02: div tags, images, and other detritus are removed, like that whole "Viserys pillages Westeros with Dothraki then Aegon swoops in and is the hero" thing: You destroy first so you can build anew.  Getting rid of the earlier structure and replacing it is easier than adapting it to your needs.

3. 02-03: The chapter content of the books is extracted.  Front and back matter of all sorts are removed, as well as all stories before and after the novellas in their anthologies.  Novels are left with nothing but chapter titles and chapter bodies, and novellas are left with nothing but their bodies.

4. 03-04: Single right quotes that function as apostrophes are replaced by keyboard apostrophes (') in order to correctly ignore those curly single right quotes as the punctuation marks they are instead of paying attention to them as legitimate word characters like the apostrophes in contractions.

4. 04-05: The books are split into files for individual chapters, complete with HTML header stuff and previous/next-chapter navigation tables above and below the chapter bodies.

5. 05-06: The HTML chapters are converted to plain text files--no markup at all--ready to be processed.

6. 06-07: The plain text chapters are read into memory and scanned over and over to exhaustively find all phrases of each size (up to 217, the largest number of words at which a phrase occurs more than once in the corpus). The phrases that occur more than once are recorded in files.

7. 07-08: The repeated phrases found in the previous step are compared to each other to eliminate those phrase-instances that are subsumed by an instance of a larger phrase, such as all the fragments of the 217-word overlap between AFFC Samwell I and ADWD Jon II that are just parts of that overlap.  Those obviously aren't as meaningful as the bigger repeated phrase.

8. 08-09: The few phrase-instances that passed the previous test but were left stranded and alone by it are eliminated.

9. 09-10: The phrase-instances that passed the previous test are reinterpreted as information for the positioning and attributes/values of HTML anchor tags that link each phrase-instance to the next instance of the same phrase (wrapping around to the earliest instance from the last one).

10. 10-11: That anchor-data is used to add links to the HTML chapter files from several steps ago.

11. 11-12: The link-bearing HTML chapter files get the prev-chapter and next-chapter links in their head/foot tables set, finally allowing you to just *turn the page* from one chapter to the next.

---
##Some "don't"s

* Don't rename **repeatedphrases.jar**.  

>“Theon,” he repeated. “My name is Theon. You have to know your name.”  
--ADWD, Chapter 62, The Sacrifice

The way I made this application work, it needs to know the name of its own .jar.  Normally, you don't need that--you just double-click and the main class in the jar kicks in and goes.  But here, the main class has to dive back into its own jar and activate a different class using some command-line arguments to give that other class's process access to enough RAM to get over the memory-hump in the middle of the "Chapterize [...]" process.