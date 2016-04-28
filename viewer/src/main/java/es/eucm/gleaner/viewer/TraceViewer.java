/**
 * Copyright (C) 2015 eUCM Research Group (e-adventure-dev@e-ucm.es)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package es.eucm.gleaner.viewer;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter.OutputType;

import es.eucm.gleaner.tracker.C;
import es.eucm.gleaner.tracker.Tracker;
import es.eucm.gleaner.tracker.Tracker.TraceListener;
import es.eucm.gleaner.tracker.format.XAPIFormat;

public class TraceViewer extends Table implements TraceListener, C {

	private XAPIFormat xapiFormat;

	private Skin skin;

	private LabelStyle traceStyle;

	private VerticalGroup feed;

	private VerticalGroup detailed;

	private ScrollPane detailedScroll;

	private int count = 0;

	public TraceViewer(Skin skin, Tracker tracker, XAPIFormat xapiFormat) {
		this.skin = skin;
		this.xapiFormat = xapiFormat;

		ButtonStyle style = skin.get("trace", ButtonStyle.class);
		if (style == null) {
			throw new GdxRuntimeException(
					"TraceViewer needs a ButtonStyle named 'trace'");
		}

		Stack stack = new Stack();

		ScrollPaneStyle scrollPaneStyle = new ScrollPaneStyle();
		scrollPaneStyle.background = skin.getDrawable("blank");
		detailedScroll = new ScrollPane(detailed = new VerticalGroup(),
				scrollPaneStyle);
		detailed.fill();
		detailed.left();

		detailedScroll.setScrollingDisabled(true, false);

		stack.addActor(detailedScroll);
		detailedScroll.setVisible(false);

		Button showDetail = new Button(style);
		showDetail.addListener(new ClickListener() {

			boolean visible = false;

			@Override
			public void clicked(InputEvent event, float x, float y) {
				visible = !visible;
				detailedScroll.setVisible(visible);
				feed.setVisible(!visible);
			}
		});
		Container<Actor> showDetailContainer = new Container<Actor>(showDetail);
		showDetailContainer.right().top();
		stack.addActor(showDetailContainer);

		stack.addActor(feed = new VerticalGroup());
		feed.right();
		feed.addActor(new Container<Actor>().minHeight(80));

		add(stack).expand().right().top();
		row();

		traceStyle = skin.get("trace", LabelStyle.class);
		if (traceStyle == null) {
			throw new GdxRuntimeException(
					"TraceViewer needs a LabelStyle named 'trace'");
		}

		tracker.addTraceListener(this);
	}

	@Override
	public void trace(String trace) {
		if (!xapiFormat.isReady()) {
			return;
		}

		String[] parts = trace.split(",");

		String action = parts[1];
		String target = parts[2];
		String value = parts.length >= 4 ? parts[3] : null;

		String text = "";

		text += "[#607d8b]" + action + "[] ";
		if (value != null) {
			String connector = "in";
			text += "[#ff9800]" + value + "[] [#607d8b]" + connector + "[] ";
		}

		text += "[#673ab7]" + target + "[]";

		Label label = new Label(text, traceStyle);
		label.setTouchable(Touchable.disabled);
		feed.addActor(label);
		label.addAction(Actions.sequence(Actions.delay(2.0f),
				Actions.fadeOut(1.0f), Actions.removeActor()));

		Table xAPI = new Table();
		xAPI.add(new Label("[#000000]" + ++count + "[]", traceStyle));
		xAPI.add(new Label(text, traceStyle)).left().expandX();
		xAPI.row();
		xAPI.add();

		LabelStyle style = skin.get("xapi", LabelStyle.class);
		if (style == null) {
			style = traceStyle;
		}

		JsonValue statement = new Json().fromJson(null,
				xapiFormat.createStatement(trace));
		xAPI.add(new Label(statement.prettyPrint(OutputType.json, 0), style))
				.expandX().fillX();

		detailed.addActor(xAPI);

	}
}
