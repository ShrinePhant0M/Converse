package edu.lehigh.converse.conversationbuilder;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import edu.lehigh.converse.R;
import edu.lehigh.converse.conversation.CNode;

public class CNodeAdapter extends ArrayAdapter<CNode>
	{
		private ArrayList<CNode>	items;
		private Context				context;

		public CNodeAdapter(Context context, int textViewResourceId, ArrayList<CNode> items)
			{
				super(context, textViewResourceId, items);
				this.items = items;
				this.context = context;
			}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
			{
				View v = convertView;
				if(v == null)
					{
						LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
						v = vi.inflate(R.layout.list_conversation_builder_item, null);
					}
				CNode c = items.get(position);
				if(c != null)
					{
						TextView id = (TextView) v.findViewById(R.id.txtID);
						TextView prompt = (TextView) v.findViewById(R.id.txtPrompt);
						TextView response = (TextView) v.findViewById(R.id.txtResponse);

						if(id != null)
							{
								id.setText("" + c.getId());
							}

						if(prompt != null)
							prompt.setText('\"' + c.getPrompt() + '\"');

						if(response != null)
							response.setText(c.getResponses().size() + " " + getContext().getString(R.string.responses));
					}
				return v;
			}
	}
