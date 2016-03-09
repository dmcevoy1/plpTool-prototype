package edu.asu.plp.tool.prototype.view.menu.options;

import com.google.common.base.Joiner;
import edu.asu.plp.tool.prototype.model.OptionSection;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * Created by Morgan on 2/28/2016.
 */
public class OptionsSettingsTree extends BorderPane
{
	private TreeView<String> sections;
	private Set<OptionSection> optionSet;
	/**
	 * Returns the item selected, and the root most parent of the item selected
	 */
	private BiConsumer<OptionSection, OptionSection> onSectionDoubleClicked;

	public OptionsSettingsTree( Set<OptionSection> sectionsList )
	{
		optionSet = sectionsList;
		sections = createEmptyRootedProjectTree();
		sections.setOnMouseClicked(this::onTreeClick);
		setCenter(sections);

		populateSectionsTree(sectionsList);
	}

	public void setTreeDoubleClick( BiConsumer<OptionSection, OptionSection> onSectionDoubleClicked )
	{
		this.onSectionDoubleClicked = onSectionDoubleClicked;
	}

	private void populateSectionsTree( Set<OptionSection> sectionsList )
	{
		for ( OptionSection section : sectionsList )
		{
			//TODO this feels disgusting. Fix this
			if ( !section.getFullPath().contains(".") )
			{
				TreeItem<String> sectionItem = new TreeItem<>(section.getName());
				if ( section.size() > 0 )
					sectionItem.getChildren().addAll(getSectionChildren(section));

				sections.getRoot().getChildren().add(sectionItem);
			}
		}
	}

	private List<TreeItem<String>> getSectionChildren( OptionSection section )
	{
		List<TreeItem<String>> subSectionList = new ArrayList<>();

		for ( OptionSection subSection : section )
		{
			TreeItem<String> subSectionItem = new TreeItem<>(subSection.getName());
			if ( subSection.size() > 0 )
				subSectionItem.getChildren().addAll(getSectionChildren(subSection));

			subSectionList.add(subSectionItem);
		}
		return subSectionList;
	}

	private void onTreeClick( MouseEvent event )
	{
		if ( event.getClickCount() == 2 )
		{
			TreeItem<String> selectionItem = sections.getSelectionModel().getSelectedItem();

			if ( onSectionDoubleClicked != null && selectionItem != null )
			{
				TreeItem<String> rootParentItem = findRootParent(selectionItem);

				OptionSection rootParent = getOptionFromTree(rootParentItem.getValue());
				OptionSection selection = getOptionFromTree(rootParent, findChildFullPath(selectionItem));

				onSectionDoubleClicked.accept(selection, rootParent);
			}
		}
	}

	/**
	 * Returns the child OptionSection of the rootParent, represented by the string parameter.
	 *
	 * @param optionParent
	 * @param childFullPath
	 * 		full path of child
	 *
	 * @return
	 */
	private OptionSection getOptionFromTree( OptionSection optionParent, String childFullPath )
	{
		if ( optionParent.getFullPath().equals(childFullPath) )
			return optionParent;

		for ( OptionSection child : optionParent )
		{
			if ( child.getFullPath().equals(childFullPath) )
				return child;
			else if ( childFullPath.startsWith(child.getFullPath()) )
			{
				return getOptionFromTree(child, childFullPath);
			}
		}

		throw new IllegalStateException("Requested option section name not found in provided collection.");
	}

	/**
	 * Retrieve a root OptionSection (OptionSection in the first level of the tree.
	 *
	 * @param rootName
	 *
	 * @return
	 */
	private OptionSection getOptionFromTree( String rootName )
	{
		for ( OptionSection section : optionSet )
		{
			if ( section.getFullPath().equals(rootName) )
				return section;
		}

		throw new IllegalStateException("Requested option section name not found in provided collection.");
	}

	private String findChildFullPath( TreeItem<String> selection )
	{
		return findChildFullPath(selection, selection.getValue());
	}

	private String findChildFullPath( TreeItem<String> selection, String childPath )
	{
		TreeItem<String> parent = selection.getParent();
		if ( parent != null && parent.getValue().length() > 0 )
			return findChildFullPath(parent, Joiner.on(".").join(parent.getValue(), childPath));
		else if ( parent.getValue().length() <= 0 )
			return childPath;
		else
			throw new IllegalArgumentException(
					"OptionsSettingsTree does not support a null root or a root with a value");
	}

	private TreeItem<String> findRootParent( TreeItem<String> selection )
	{
		TreeItem<String> parent = selection.getParent();
		//Reached root
		if ( parent != null && parent.getValue().length() > 0 )
			return findRootParent(parent);
		else if ( parent.getValue().length() <= 0 )
			return selection;
		else
			throw new IllegalArgumentException(
					"OptionsSettingsTree does not support a null root or a root with a value");
	}

	private TreeView<String> createEmptyRootedProjectTree()
	{
		TreeItem<String> root = new TreeItem<String>("");
		root.setExpanded(true);

		TreeView<String> treeView = new TreeView<String>(root);
		treeView.showRootProperty().set(false);
		treeView.setBackground(Background.EMPTY);

		return treeView;
	}
}
