#RepeatedPhrases  
##A tool for boiled leather and insights

Some phrases are repeated in A Song of Ice and Fire.  Some [are suggestive as hell.](http://redd.it/30y8ez)  Most [are not.](http://imgur.com/75joFxC)  A few [are in the middle.](http://imgur.com/z789AIe)  Really the only way to know which is the case is [to view them in context](http://imgur.com/YbHU0zS), and that's exactly what I'm giving you to the power to do.

This tool lets you start with ebooks of ASOIAF and end up with individual chapter files that provide clickable links from one instance of a repeated phrase to the next so you can easily determine something's significance based on its use in its original context and its use in another context. [Here's a demonstration](http://imgur.com/bqX7mpJ) pertaining to the repeated phrase mentioned in the first link in the first paragraph. Making the chapters independent makes it easier to read in a creative order, such as the famed [Boiled Leather](http://boiledleather.com/post/24543217702/a-proposed-a-feast-for-crows-a-dance-with-dragons) order.

Report bugs, horrible crashes, etc. at reddit-link-goes-here.

---
##Requirements

| Requirement | Purpose | Note |
|:--|:--|:--|
| ASOIAF ebooks |  | Ignore "The World of Ice and Fire" |
| [Calibre](http://calibre-ebook.com/) | Convert the ebooks (mobi, epub, or other format) to HTML | Other conversion software is fine. It's free. |
| [7-Zip](http://www.7-zip.org/download.html) | Unzip this repository after downloading; unzip the HTMLZ files produced by Calibre when converting to HTML | It's free. |
| [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html) | Run the code | It's free. Java compiles to bytecode instead of pure machine code. I used lambda expressions, which Java 7 doesn't support |
| 400MB (~ish) RAM available | An increased amount of memory is needed during repeated-phrase analysis |  |

---
##Instructions

* Download and install [Calibre](http://calibre-ebook.com/) (if you don't already have it).  
* Download ebooks for the ASOIAF novels and novellas (if you don't already have them).  
* Add the ebooks to your Calibre library (if they're not already in there).  
* Convert them to HTMLZ.  
* Go to the folders where those HTMLZ files are located (In Calibre, right click the book and choose "Open containing folder".) and unzip them (to their own new folders, preferably)
* Choose/Create a work folder somewhere. It's called **work_folder** in these instructions, but you can name it whatever you want.
* [Download the project as a ZIP](https://github.com/fiveham/Repeated_Phrases/archive/master.zip), unzip it, and copy/move **repeatedphrases.jar** to **work_folder**
* **NOTE** do not rename **repeatedphrases.jar**. Normally that isn't an issue, but it is in this case because this application needs to know the name of the jar it's launched out of to launch correctly (or at all).
* Double-click the **repeatedphrases.jar** file in **work_folder** to run the application.  

After a few seconds, a small, poorly-designed window will pop up, named ["Repeated Phrase Analyser".](http://imgur.com/9HgQFen) The second line from the top of the window names the folder from/in which the application is operating. If this isn't your chosen **work_folder**, then something's wrong (with my code).

* Click "Create Folders".

This makes the folders that this application needs in order to work. Once these folders have been created, copy each ebook's unzipped **index.html** file (still in various folders in the Calibre library) into **00_html_books** and rename them so they indicate what book they belong to. The exact names they need to have for the application to work are:

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

**Note**: If you don't have all of these books, it doesn't matter, the application will run just fine and produce results that pertain only to the books you do have.

* Specify a trail-file (I recommend moving at least one from the unzipped project into **work_folder**), optionally specify a minimum word-count for phrases to get linked (defaults to 3), and click "Chapterize books; Add links".

This will take most of an hour to run.

If you don't specify a minimum linked phrase size, it'll default to 3, which is a decent balance, I think, eliminating a lot of completely pointless links that make the final product difficult to read. You can even set it to something over 218 to eliminate all repeated-phrase links and leave yourself with nothing but pure chapters (not recommended).

Once it finishes, the GUI's Status will start with "Done:".

* Copy **style.css** from this project into **12_readable** and open the html chapters in a browser.

If the style (such as the text colors) is not to your liking, you can open **style.css** in a text editor and change it.  For example, changing the **color: #??????;** values in the CSS file (in the section titled /\*GENERAL CONCERNS\*/) to some other colors, such as **color: blue;** or **color: white;**.  Don't forget the **semicolon** (;).

---
##Switching to/from Boiled Leather, Ball of Beasts, etc.

If and when you choose to read with a different chapter order or with a different limit for the linked phase size, run the application, specify a trail-file that has the chapter-order you want, and click "Change Chapter Order" or "Change Trail (Keep Link Order)" if you don't care about the chapter sequence assumed by the in-text links.

This should finish in less than ten minutes. It doesn't matter if you choose a different minimum phrase size than was used for the second-button operation, because that value isn't used until the part of that process that activates this very process. Data for all phrase sizes will have been generated and this process tries to add links for all of them to the chapter files, but only those that meet the minimum word-count get used.

---
##Trail-file format

A trail file, used to specify the order of chapters and to identify which chapters are linked as previous and next chapters from each chapter, is made of three tab-delimited columns: Previous Chapter; Current Chapter; Next Chapter  
The entries are specified as .html filenames for the chapters affected.

I chose this redundant format to make it possible to control whether you want the first/last chapter to link to the last/first, or whatever, rather than forcing everything to link or to not link. I've included a trail file (**individual_books.txt**) that takes advantage of this (if you can call it that) to make each book's final and first chapters not link to a next or previous (respectively) chapter. This format also allows for chapters to be connected in multiple loops, which **char_blocks.txt** uses, for example.

With Excel, it's pretty easy to just copy and past the central column on the left and right side, shifted down or up one cell as needed and then save as tab-delimited text; so, feel free to make your own.

---
##Notes on the application's operation

After the folders 00_ through 12_ are created, this application's main operation ("Chapterize [...]") is a series of island-hops from one folder to the next, transforming the content of the books in distinct steps.

1. 00-01: Newlines are added to ensure mild human-readability if you choose to look into the source HTML (like for messing with CSS classes). This also helps step 4 go faster by shortening line lengths, since it goes line-by-line.

2. 01-02: div tags, images, and other detritus are removed, like that whole "Viserys pillages Westeros with Dothraki then Aegon swoops in and is the hero" theory: You destroy first so you can build anew. Getting rid of the earlier structure and replacing it is easier than adapting the earlier structure to your needs.

3. 02-03: The chapter content of the books is isolated. Front and back matter of all sorts are removed, including all stories before and after the ASOIAF novellas in their anthologies.  Novels are left with nothing but chapter titles and chapter bodies, and novellas are left with nothing but their bodies.

4. 03-04: Single right quotes that function as apostrophes are replaced by keyboard apostrophes (') in order to correctly ignore those curly single right quotes as the punctuation marks they are while easily identifying word-acceptable apostrophes.

4. 04-05: The books are split into individual chapters.

5. 05-06: The HTML chapters are converted to plain text files--no markup at all--ready to be processed.

6. 06-07: The plain text chapters are read into memory and scanned over and over to exhaustively find all phrases of each size (up to a per-phrase word-count of 217, the largest number of words at which a phrase occurs more than once in the corpus). The phrases that occur more than once are recorded in files.

7. 07-08: The repeated phrases found in the previous step are compared to each other to eliminate those phrase-instances that are subsumed by an instance of a larger phrase, such as all the fragments of the 217-word overlap between AFFC Samwell I and ADWD Jon II that are just parts of that overlap. Those obviously aren't as meaningful as the bigger repeated phrase.

8. 08-09: The few phrase-instances that passed the previous test but were left stranded and alone by it are eliminated.

9. 09-10: The phrase-instances that passed the previous test are reinterpreted as information for the positions and attributes/values of HTML anchor tags that link each phrase-instance to the next instance of the same phrase (wrapping around to the earliest instance from the last one).

10. 10-11: That anchor-data is used to add links to the HTML chapter files produced in step 4.

11. 11-12: The link-bearing HTML chapter files get the prev-chapter and next-chapter links in their head/foot tables set, finally allowing you to just *turn the page* from one chapter to the next.

The third button ("Change Chapter Order")'s class is responsible for performing steps 9, 10, and 11.

The fourth button ("Change Trail")'s class is responsible for performing step 11.

---
##Some "don't"s

* Don't rename **repeatedphrases.jar**.  

>&ldquo;Theon,&rdquo; he repeated. &ldquo;My name is Theon. You have to know your name.&rdquo;  
&mdash;ADWD, Chapter 62, The Sacrifice

The way I made this application work, it needs to know the name of its own .jar.  Normally, you don't need that--you just double-click and the main class in the jar kicks in and goes.  But here, the main class has to dive back into its own jar and [activate a different class using some command-line arguments](http://docs.oracle.com/javase/7/docs/api/java/lang/Runtime.html#exec(java.lang.String[])) to give that other class's process access to enough RAM to get over the memory-hump in the middle of the "Chapterize [...]" process.

---
##License

Copyright (c) 2015

This project is licensed under the terms of the [MIT license.](http://choosealicense.com/licenses/mit/)