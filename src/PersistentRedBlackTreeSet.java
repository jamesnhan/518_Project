import java.util.*;

/**
 * An unmutable ordered set that forks modified versions (insert, delete) in log(n) space and time 
 * <p>
 * Red-black tree set keeps member elements in order according to the supplied comparator, or the hashcode.
 * <p>
 *
 * The implementation does not conform to java API Collections contract, because this does not support persistence. The
 * class overrides AbstractSet so that this set is compatible with other collection helpers
 * <p>
 *
 * Insert, delete, membership queries and lookups are all log(n) operations in time and space, but with persistence. This
 * implementation is optimized for speed. Persistence was added as per the paper, Making Data Structures Persistent,
 * by James R. Driscoll ,  Neil Sarnak ,  Daniel D. Sleator ,  Robert E. Tarjan
 * <p>
 *
 * A persistent red-black tree forks a new version of itself for every operation.
 * The returned value from the operation method (insert(*), delete(*)) is newly modified version, while the original
 * reference stays structurally the same
 * <p>
 *
 * <code>
 * e.g.                                                          <br />
 * PersistentRedBlackTreeSet a = new PersistentRedBlackTreeSet();<br />
 * PersistentRedBlackTreeSet a2 = a1.insert(new Object());       <br />
 * assert(a.isEmpty()); assert(!a2.isEmpty);                     <br />
 * </code>
 * 
 * <p>
 * This is very useful for search if the state of variables change little between transitions. The entire state does
 * not need to be copied into a new state data structure for modification (which would normally cost O(n)).
 * <p><a href="http://rtm.science.unitn.it/reactive-search/thebook/node36.html">Hashing combined with persistent red-black trees,  R. Battiti, M. Brunato, and F. Mascia</a>
 * <p><a href="http://en.wikipedia.org/wiki/Red-black_tree">Red-black trees,  wikipedia</a>
 *
 * <p>
 * I have since found this collection to be very useful as a building block for other complex persistent data structures like
 * persistent graphs. This was used in my PhD thesis and has had about 3 years of bashing sums on it. I think most bugs have
 * been squished.
 *
 * <p>
 * Copyright 2009 Tom Larkworthy
 * This program is distributed under the terms of the GNU General Public License as per http://www.gnu.org/licenses/gpl.txt Version 3, 29th June 2007
 * Other licensing options are available (email tom.larkworthy att spectral-robotics.com)
 * @author Tom.Larkworthy@spectral-robotics.com
 */
public class PersistentRedBlackTreeSet<D> extends AbstractSet<D> implements Iterable<D>{
	private static final boolean RED = false;
	private static final boolean BLACK = true;

	private static final Comparator<Object> HASH_MAP_COMPARATOR = new Comparator<Object>(){

		public int compare(Object o1, Object o2) {
			return o1.hashCode() - o2.hashCode();
		}
	};


	Comparator<? super D> comparator;

	RedBlackNode<D> root = new RedBlackNode<D>((RedBlackNode<D>)null);
	RedBlackNode<D> newRoot = null;

	int size = 0;

	private long longHashcode=0; //addition hashcode calculated in a different way to try and avoid clashes
	int hashcode;   //simple hascode

	private ArrayList<D> elements = null;

	public PersistentRedBlackTreeSet() {
		this.comparator = HASH_MAP_COMPARATOR;
	}

	public PersistentRedBlackTreeSet(Comparator<? super D> comparator) {
		this.comparator = comparator;
	}

	private PersistentRedBlackTreeSet(Comparator<? super D> comparator, RedBlackNode<D> root, int size, int hashcode, long longHashcode) {
		this.size =size;
		this.comparator = comparator;
		this.root = root;
		this.hashcode = hashcode;
		this.longHashcode = longHashcode;
	}

	private RedBlackNode<D> rotate_right(LinkedList<RedBlackNode<D>> parents, RedBlackNode<D> n) {
		RedBlackNode<D> left = n.left ;//= n.left.clone();
		RedBlackNode<D> right = n.right;// = n.right.clone();
		RedBlackNode<D> left_right = n.left.right;// = n.left.right.clone();
		RedBlackNode<D> left_left = n.left.left;// = n.left.left.clone();

		//make left the new top node
		if(n.parent(parents)!= null){
			if(n.parent(parents).left == n) n.parent(parents).left = left;
			else n.parent(parents).right = left;
		}else{
			newRoot = left;
		}

		//make n the new right child of the top node
		left.right = n;

		//swap parent of left_right
		n.left = left_right;

		n.right = right;
		return left;
	}

	private RedBlackNode<D> rotate_left(LinkedList<RedBlackNode<D>> parents, RedBlackNode<D> n) {
		RedBlackNode<D> right = n.right;// = n.right.clone();
		RedBlackNode<D> left = n.left;// = n.left.clone();
		RedBlackNode<D> right_left = n.right.left;//= n.right.left.clone();
		RedBlackNode<D> right_right = n.right.right;// = n.right.right.clone();

		//make right the new top node
		if(n.parent(parents)!= null){
			if(n.parent(parents).left == n) n.parent(parents).left = right;
			else n.parent(parents).right = right;
		}else{
			newRoot = right;
		}

		//make n the new left child of the top node
		right.left = n;

		//swap parent of left_right
		n.right = right_left;

		n.left = left;
		return right;
	}

	private void replace_node(RedBlackNode<D> node,RedBlackNode<D> nodeParent, RedBlackNode<D> replacement) {
		if(nodeParent != null){
			if(nodeParent.left == node) nodeParent.left = replacement;
			else {
				nodeParent.right = replacement;
			}
		}else{
			newRoot = replacement;
		}
	}

	/**
	 * inserts an element persistently. "this" remains the same list. The returned list is the modified version
	 * @param element the new data element
	 * @return the "this" with "element" removed
	 */
	public PersistentRedBlackTreeSet<D> insert(D element) {
		RedBlackNode<D> newNode = new RedBlackNode<D>(element);
		return insertNode(newNode);
	}

	/**
	 * note the subtree is not merged, only inserted into a specific position within the tree, all subelements of the
	 * subtree remain in the same order. Use with caution.
	 * @param subtree
	 * @return
	 */
	public PersistentRedBlackTreeSet<D> insertSubTree(PersistentRedBlackTreeSet<D> subtree) {
		if(subtree.size() == 0) return this;
		return insertNode(subtree.root);
	}


	private PersistentRedBlackTreeSet<D> insertNode(RedBlackNode<D> newNode) {
		newRoot = root.clone();
		RedBlackNode<D> current = newRoot;


		LinkedList<RedBlackNode<D>> parents = new LinkedList<RedBlackNode<D>>();
		parents.add(null);//null indicates the root parent, just like it does in RedBlackTree

		//find the node where the element should go at the bottom of the tree
		while (current.element != null) {
			parents.add(current);
			int dir = comparator.compare(newNode.element, current.element);
			if(dir == 0) {
				newRoot = null;   //allready in tree
				return this;
			} else if (dir < 0) {
				current = current.left = current.left.clone();
			} else {
				current = current.right = current.right.clone();
			}
		}

		if(parents.getLast() != null){
			if (parents.getLast().left == current) {
				parents.getLast().left = newNode;
			} else {
				assert parents.getLast().right == current;
				parents.getLast().right = newNode;
			}
		}


		insertCase_1(parents, newNode);


		PersistentRedBlackTreeSet<D> result =  new PersistentRedBlackTreeSet<D>(comparator, newRoot, size+1, hashcode ^ newNode.element.hashCode(), longHashcode + newNode.element.hashCode());
		newRoot = null;//clear reference to the newly generated root
		return result;
	}

	private void insertCase_1(LinkedList<RedBlackNode<D>> parents, RedBlackNode<D> n) {
		if (parents.getLast() == null){
			newRoot = n;
			n.black = BLACK;
		}
		else
			insertCase_2(parents, n);
	}

	private void insertCase_2(LinkedList<RedBlackNode<D>> parents, RedBlackNode<D> n) {
		//easy case 2
		if (parents.getLast().black){

		}else{
			insertCase_3(parents, n);
		}
	}

	private void insertCase_3(LinkedList<RedBlackNode<D>> parents, RedBlackNode<D> n) {
		if (!n.uncle(parents).black) {
			parents.getLast().black = BLACK;
			n.cloneUncle(parents);
			n.uncle(parents).black = BLACK;
			n.grandparent(parents).black = RED;
			//we recurse on the granparent now, up two levels
			//so we need to pop a 2 elements from the parents list
			RedBlackNode<D> grandparent = n.grandparent(parents);
			parents.removeLast();
			parents.removeLast();
			insertCase_1(parents, grandparent);
		} else
			insertCase_4(parents, n);

	}

	private void insertCase_4(LinkedList<RedBlackNode<D>> parents, RedBlackNode<D> n) {
		if (n == n.parent(parents).right && n.parent(parents) == n.grandparent(parents).left) {
			//we rotate on n's parent
			//so we need to pop n's parent off the parent list
			RedBlackNode<D> nParent = n.parent(parents);
			parents.removeLast();
			RedBlackNode<D> replacement = rotate_left(parents, nParent);
			//then we go to case 5 using n's left. So n is the new parent of the algorithm's n
			parents.addLast(replacement);
			n = replacement.left = replacement.left.clone();
		} else if (n == n.parent(parents).left && n.parent(parents) == n.grandparent(parents).right) {
			RedBlackNode<D> nParent = n.parent(parents);
			parents.removeLast();
			RedBlackNode<D> replacement = rotate_right(parents, nParent);
			parents.addLast(replacement);
			n = replacement.right = replacement.right.clone();
		}
		insert_case5(parents, n);
	}


	private void insert_case5(LinkedList<RedBlackNode<D>> parents, RedBlackNode<D> n) {
		n.parent(parents).black = BLACK;
		n.grandparent(parents).black = RED;
		if (n == n.parent(parents).left && n.parent(parents) == n.grandparent(parents).left) {
			RedBlackNode<D> gp = n.grandparent(parents);
			parents.removeLast();
			parents.removeLast();
			rotate_right(parents, gp);
		} else {
			assert n == n.parent(parents).right && n.parent(parents) == n.grandparent(parents).right;
			RedBlackNode<D> gp = n.grandparent(parents);
			parents.removeLast();
			parents.removeLast();
			rotate_left(parents, gp);
		}
	}

	/**
	 * returns a new list, that is "this" minus the deleted item.
	 * @param element the element to delete
	 * @return the modified version
	 */
	public PersistentRedBlackTreeSet<D> delete(D element){
		RedBlackNode<D> current = newRoot = root.clone();
		//RedBlackNode<D> current = newRoot = root.deepClone();
		LinkedList<RedBlackNode<D>> parents = new LinkedList<RedBlackNode<D>>();
		parents.add(null);//root represented by null

		
		//find the node where the element should go at the bottom of the tree
		while (current.element != null) {
			parents.add(current);

			int dir = comparator.compare(element, current.element);
			if (dir < 0) {
				current = current.left = current.left.clone();
			} else if(dir > 0){
				current = current.right= current.right.clone();
			}else{

				parents.removeLast();
				delete(parents, current);
				PersistentRedBlackTreeSet<D> tree = new PersistentRedBlackTreeSet<D>(comparator, newRoot, size-1, hashcode ^ element.hashCode(), longHashcode - element.hashCode());
				newRoot = null;
				return tree;
			}
		}
		newRoot = null;
		//no change
		return this;
	}

	private void delete(LinkedList<RedBlackNode<D>> parents, RedBlackNode<D> n) {

		assert n.parent(parents) == null || n.parent(parents).left == n || n.parent(parents).right == n;

		//if the node has two children, we swap it with the next leaf
		if(n.left.element != null && n.right.element != null){
			parents.addLast(n);
			n.left = n.left.clone();
			RedBlackNode<D> current = n.right = n.right.clone();
			while(current.element != null){
				parents.addLast(current);
				current = current.left = current.left.clone();
			}

			n.element = current.parent(parents).element;

			RedBlackNode<D> currentParent = current.parent(parents);
			parents.removeLast();
			delete_one_child(parents, currentParent);
		}else{
			delete_one_child(parents, n);
		}
	}

	private void delete_one_child(LinkedList<RedBlackNode<D>> parents, RedBlackNode<D> n) {
		/* Precondition: n has at most one non-null child */
		RedBlackNode<D> child;
		if(n.right.element == null){
			child = n.left = n.left.clone();
		}else{
			assert n.left.element == null;
			child = n.right = n.right.clone();
		}

		replace_node(n, n.parent(parents), child);
		if (n.black) {
			if (!child.black)
				child.black = BLACK;
			else
				delete_case1(parents, child);
		}
	}


	private void delete_case1(LinkedList<RedBlackNode<D>> parents, RedBlackNode<D> n) {
		if (n.parent(parents) == null)
		{
			newRoot = n;
		}else{
			delete_case2(parents, n);
		}
	}

	private void delete_case2(LinkedList<RedBlackNode<D>> parents, RedBlackNode<D> n) {
		if (!n.sibling(parents).black) {
			n.cloneSibling(parents);
			n.parent(parents).black = RED;
			n.sibling(parents).black = BLACK;
			if (n == n.parent(parents).left)
			{
				//rotate on the parent, back up one level
				RedBlackNode<D> parent = n.parent(parents);
				parents.removeLast();
				RedBlackNode<D> replacement = rotate_left(parents, parent);

				//n acually ends up deeper now than to begin with
				//so we need to regenerate the parents list to its prior level
				parents.add(replacement);
				parents.add(replacement.left);
				n = replacement.left.left = replacement.left.left.clone();
			}
			else
			{
				RedBlackNode<D> parent = n.parent(parents);
				parents.removeLast();
				RedBlackNode<D> replacement =rotate_right(parents, parent);
				parents.add(replacement);
				parents.add(replacement.right);
				n = replacement.right.right = replacement.right.right.clone();
			}
		}
		delete_case3(parents, n);
	}

	private void delete_case3(LinkedList<RedBlackNode<D>> parents, RedBlackNode<D> n) {
		if (n.parent(parents).black &&
				n.sibling(parents).black &&
				n.sibling(parents).left.black &&
				n.sibling(parents).right.black) {
			n.cloneSibling(parents);
			n.sibling(parents).black = RED;

			RedBlackNode<D> parent = parents.removeLast();
			delete_case1(parents, parent);
		} else
			delete_case4(parents, n);
	}

	private void delete_case4(LinkedList<RedBlackNode<D>> parents, RedBlackNode<D> n) {
		if (!n.parent(parents).black &&
				n.sibling(parents).black &&
				n.sibling(parents).left.black &&
				n.sibling(parents).right.black) {
			n.cloneSibling(parents);
			n.sibling(parents).black = RED;
			n.parent(parents).black = BLACK;
		} else
			delete_case5(parents, n);
	}

	private void delete_case5(LinkedList<RedBlackNode<D>> parents, RedBlackNode<D> n) {
		if (n == n.parent(parents).left &&
				n.sibling(parents).black &&
				!n.sibling(parents).left.black &&
				n.sibling(parents).right.black) {
			n.cloneSibling(parents);
			n.sibling(parents).black = RED;
			n.sibling(parents).left = n.sibling(parents).left.clone();
			n.sibling(parents).left.black = BLACK;
			rotate_right(parents, n.sibling(parents));
		} else if (n == n.parent(parents).right &&
				n.sibling(parents).black &&
				!n.sibling(parents).right.black &&
				n.sibling(parents).left.black) {
			n.cloneSibling(parents);
			n.sibling(parents).black = RED;
			n.sibling(parents).right = n.sibling(parents).right.clone();
			n.sibling(parents).right.black = BLACK;
			rotate_left(parents, n.sibling(parents));
		}
		delete_case6(parents, n);
	}

	private void delete_case6(LinkedList<RedBlackNode<D>> parents, RedBlackNode<D> n) {
		n.cloneSibling(parents);
		n.sibling(parents).black = n.parent(parents).black;
		n.parent(parents).black = BLACK;
		if (n == n.parent(parents).left) {
			n.sibling(parents).right = n.sibling(parents).right.clone();
			n.sibling(parents).right.black = BLACK;
			RedBlackNode<D> parent = parents.removeLast();
			rotate_left(parents, parent);
		} else {
			n.sibling(parents).left = n.sibling(parents).left.clone();
			n.sibling(parents).left.black = BLACK;
			RedBlackNode<D> parent = parents.removeLast();
			rotate_right(parents, parent);
		}
	}

	private ArrayList<D> fillArray(ArrayList<D> array, RedBlackNode<D> current){
		if(current.element == null) return array;//null element

		fillArray(array, current.left);
		array.add(current.element);
		fillArray(array, current.right);
		return array;
	}

	/**
	 * returns the contents of the PRBTree in a list, the result is cached and returned on subsequent call.
	 * DO NOT MODIFY THE RETURNED LIST, copy it modification is needed.
	 */
	public List<D> getElements(){
		if(elements == null){
			elements = new ArrayList<D>(size);
			fillArray(elements, root);
		}
		return elements;
	}

	/**
	 * iterates the elements of the list. O(1) cost of initialisation and next(). You can abandon
	 * iteratation midway without time or space penalties. 
	 * @return
	 */
	public Iterator<D> iterator() {
		return new NullIteratorAdapter<D>(new InOrderTraverser());
	}

	public int size() {
		return size;
	}

	/**
	 * gets a random leaf element out the list in log(n) time. Note some elements are not stored in leaf nodes, so
	 * not all the contents can be sampled. However, samples are approximately drawn uniformly over the full range of the list
	 * @return
	 */
	public D getRandomLeaf(){

		RedBlackNode<D> current = root;
		while (true) {
			int dir = Math.random()>.5f?1:-1;
			if(dir < 0) {
				if(current.left.element == null) return current.element;
				current = current.left;
			} else {
				if(current.right.element == null) return current.element;
				current = current.right;
			}
		}

		//return root.element;
	}

	/**
	 * log(n) implementation of contains
	 * @param e
	 * @return
	 */
	public boolean contains(Object e) {
		D element = (D) e;

		RedBlackNode<D> current = root;
		while (current.element != null) {
			int dir = comparator.compare(element, current.element);
			if (dir == 0 ){
				return true;
			}else if(dir < 0) {
				current = current.left;
			} else {
				current = current.right;
			}
		}
		return false;
	}

	public D get(D element) {
		RedBlackNode<D> current = root;
		while (current.element != null) {
			int dir = comparator.compare(element, current.element);
			if (dir == 0 ){
				return current.element;
			}else if(dir < 0) {
				current = current.left;
			} else {
				current = current.right;
			}
		}
		return null;
	}
	
	public RedBlackNode<D> GetRootNode()
	{
		return this.root;
	}


	public int hashCode() {
		return hashcode;
	}

	private D getRoot() {
		if(size == 0) return null;
		return root.element;
	}

	private class InOrderTraverser implements Iterator<D>{
		Stack<TraversalVariable> stack = new Stack<TraversalVariable>();


		public InOrderTraverser() {
			if(root.element!=null){
				stack.push(new TraversalVariable(TraversalSymbol.RIGHT, root));
				stack.push(new TraversalVariable(TraversalSymbol.VISIT, root));
				stack.push(new TraversalVariable(TraversalSymbol.LEFT, root));
			}
		}

		public boolean hasNext() {
			return true;
		}

		public D next(){
			if(stack.isEmpty()) return null;
			TraversalVariable curr = stack.pop();

			while(curr.s != TraversalSymbol.VISIT){
				RedBlackNode<D> child;
				if(curr.s == TraversalSymbol.LEFT){
					child = curr.node.left;
				}else{
					assert curr.s == TraversalSymbol.RIGHT;
					child = curr.node.right;
				}

				if(child.element != null){
					stack.push(new TraversalVariable(TraversalSymbol.RIGHT, child));
					stack.push(new TraversalVariable(TraversalSymbol.VISIT, child));
					stack.push(new TraversalVariable(TraversalSymbol.LEFT, child));
				}

				if(stack.isEmpty()) return null;
				curr = stack.pop();
			}

			return curr.node.element;

		}

		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

	private class TraversalVariable{
		TraversalSymbol s;


		public TraversalVariable(TraversalSymbol s, RedBlackNode<D> node) {
			this.s = s;
			this.node = node;
		}

		RedBlackNode<D> node;
	}

	private static enum TraversalSymbol{LEFT, RIGHT, VISIT}


	/**
	 * currently only supports comparisons with other PersistentRB sets
	 * @param obj
	 * @return
	 */
	public boolean equals(Object obj) {
		PersistentRedBlackTreeSet<D> other = (PersistentRedBlackTreeSet<D>) obj;

		if(other.hashcode != hashcode) return false;
		if(other.longHashcode != longHashcode)return false;


		List<D> l1 = other.getElements();
		List<D> l2 = getElements();

		return l1.equals(l2);
	}


	/**
	 * main storage node for class
	 * @param <D>
	 */
	public class RedBlackNode<D> {
		RedBlackNode<D> left;
		RedBlackNode<D> right;
		boolean black;
		D element;


		public RedBlackNode() {
		}

		/**
		 * creates a new RED leaf node (black empty children are also created automatically)
		 *
		 * @param element elemnt
		 */
		public RedBlackNode(D element) {
			this.element = element;
			black = RED;
			this.left = new RedBlackNode<D>(this);  //create BLACK null nodes
			this.right = new RedBlackNode<D>(this);//create BLACK null nodes
		}

		public RedBlackNode(RedBlackNode<D> parent) {
			this.black = BLACK;
		}

		RedBlackNode<D> grandparent(LinkedList<RedBlackNode<D>> parents) {
			return parents.get(parents.size() - 2);
		}

		RedBlackNode<D> parent(LinkedList<RedBlackNode<D>> parents) {
			return parents.getLast();
		}

		RedBlackNode<D> uncle(LinkedList<RedBlackNode<D>> parents) {
			if (grandparent(parents).left == parents.getLast()){
				return grandparent(parents).right;
			}
			else{
				assert grandparent(parents).right == parents.getLast();
				return grandparent(parents).left;
			}
		}

		public void cloneUncle(LinkedList<RedBlackNode<D>> parents){
			if (grandparent(parents).left == parents.getLast()){
				grandparent(parents).right = grandparent(parents).right.clone();
			}
			else{
				assert grandparent(parents).right == parents.getLast();
				grandparent(parents).left = grandparent(parents).left.clone();
			}
		}


		public RedBlackNode<D> sibling(LinkedList<RedBlackNode<D>> parents) {
			if (parent(parents).left == this){
				return parent(parents).right;
			}
			else{
				return parent(parents).left;
			}
		}


		public void cloneSibling(LinkedList<RedBlackNode<D>> parents) {
			if (parent(parents).left == this){
				parent(parents).right = parent(parents).right.clone();
			}
			else{
				assert parent(parents).right == this;
				parent(parents).left = parent(parents).left.clone();
			}
		}

		public RedBlackNode<D> clone(){
			RedBlackNode<D> node = new RedBlackNode<D>();
			node.element = element;
			node.left = left;
			node.right = right;
			node.black = black;
			return node;
		}

		public RedBlackNode<D> deepClone(){
			RedBlackNode<D> node = new RedBlackNode<D>();
			node.element = element;
			node.black = black;

			if(element == null) return node;

			node.left = left.deepClone();
			node.right = right.deepClone();
			return node;
		}
		
		public RedBlackNode<D> GetLeft()
		{
			return this.left;
		}
		
		public RedBlackNode<D> GetRight()
		{
			return this.right;
		}
		
		public D GetElement()
		{
			return this.element;
		}
	}

	/**
	 * This iterator wraps another iterator so the implementor of the other iterator can be lazy.
	 * They do not need to implement the hasNext function. The emptyness of the iterator can be signalled by returning
	 * a null from the next() method.
	 */
	private class NullIteratorAdapter<T> implements Iterator<T> {
		T next;
		Iterator<T> wrapper;


		public NullIteratorAdapter(Iterator<T> nullTerminatedIterator) {
			this.wrapper = nullTerminatedIterator;
			next = wrapper.next();
		}

		public boolean hasNext() {
			return next != null;
		}

		public T next() {
			T ret = next;
			next = wrapper.next();
			return ret;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}