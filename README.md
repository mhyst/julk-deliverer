Deliverer
=========

Transparent Distributed System written in Java

By Mhyst (Julio Cesar Serrano Ortuno)



What is Deliverer
-----------------

When we talk about a computer program, we generally think about a certain software that accomplishes some task on a computer, and that's it... That's have been the way we see software for past fifty years. Internet changed the world, but everybody has his website separated from other's websites and that's the way we want it to be, because we see the Net as a place to visit, but not to live in. The truth I see is that Internet is a powerful tool but it can be even more powerful if we change our mind a bit to the open side. Computers are all different and each one have access to different tools with different capablites based on hardware and software requirements. So why not let the programs to flow over the Net and let some computers perform some tasks while others rather do other tasks being all in the same Distributed Software? That's exactly what Deliverer was made for: to allow you to make software distributed over a private or public network. You'll write the program, but you'll never know where each piece of your program is going to be executed on.

How doest it work
---------------------------

Imagine a private network with some dozens or hundreds of computers. Imagine every of such computers have been provided with a copy of Deliverer. All it's left is to write your distributed code. For that, Deliverer gives you a java abstract class to extend. It's name, Translator:


```java
package julk.net.deliver;

public abstract class Translator
{
  protected abstract boolean translate (String user, String service, String command, WorkResult wr);
}
```



If you already have your software written, it isn't necessary to rewrite it entirely. Just create some classes extending Translator and call your modules from it. If your software isn't Java is not a great deal either.

For each class extending Translator, when translate method is called, will receive some data from the previous piece of your software and will have to return the data needed for the next piece (or phase). Every phase may occur on the same machine or in different machines in a transparent way (that will depend on Deliverer Network configuration), Deliverer will distribute the data where is needed. That simple.

In the end, thanks to Deliverer, your software is going to be run across the net. Through Deliverer configuration you tell to your network which computers are allowed to run what modules. You may tell Deliverer to be able to run the same modules on several machines. The work load will be automatically balanced among them.

For instance, if part of the work needs 3d rendering, you can configure such modules to be run by machines with better graphic performance, while tasks in need for Internet access may be assigned to computers whith Internet access. You may ever create a Deliverer network over Internet. Absolutely yes. 
